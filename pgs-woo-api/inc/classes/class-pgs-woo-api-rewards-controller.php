<?php
class PGS_WOO_API_RewardsController {
    public function get_single_product_rewards_message($product){

		$message = get_option( 'wc_points_rewards_single_product_message' );

		$points_earned = $this->pgs_woo_api_get_points_earned_for_product_purchase( $product->get_id() );

		// bail if none available
		if ( ! $message || ! $points_earned ) {

			$message = '';

		} else {

			// Check to see if Dynamic Pricing is installed
			if ( class_exists( 'WC_Dynamic_Pricing' ) ) {
				// check to see if there are pricing rules for this product, if so, use the 'earn up to X points' message
				if ( get_post_meta( $product->get_id(), '_pricing_rules', true ) ) {
					$message = $this->pgs_woo_api_create_variation_message_to_product_summary( $points_earned );
				}
			}

			// replace message variables
			$message = $this->pgs_woo_api_replace_message_variables( $message, $product, $points_earned );
		}
        if ( method_exists( $product, 'get_available_variations' ) ) {
            $message = $this->pgs_woo_api_add_variation_message_to_product_summary($product);
        }
		return $message;
    }

    /**
	 * Create the "Earn up to X" message
	 *
	 * @since 1.2.6
	 */
	public function pgs_woo_api_create_variation_message_to_product_summary( $points ) {
		global $wc_points_rewards;

		$message = get_option( 'wc_points_rewards_variable_product_message', '' );
		if ( ! empty( $message ) ) {

			// replace placeholders inside settings values
			$message = str_replace( '{points}', number_format_i18n( $points ), $message );
			$message = str_replace( '{points_label}', $wc_points_rewards->get_points_label( $points ), $message );

		}

		$message = '<p class="points">' . $message . '</p>';
		return $message;
	}

    /**
	 * Add a message about the points to the product summary
	 *
	 * @since 1.2.6
	 */
	public function pgs_woo_api_add_variation_message_to_product_summary($product) {

		// make sure the product has variations (otherwise it's probably a simple product)
		if ( method_exists( $product, 'get_available_variations' ) ) {
			// get variations
			$variations = $product->get_available_variations();

			// find the variation with the most points
			$points = $this->pgs_woo_api_get_highest_points_variation( $variations, $product->get_id() );

			$message = '';
			// if we have a points value let's create a message; other wise don't print anything
			if ( $points ) {
				$message = $this->pgs_woo_api_create_variation_message_to_product_summary( $points );
			}

			return $message;
		}
	}



	public function pgs_woo_api_get_points_earned_for_product_purchase( $product, $order = null ) {

        // if we don't have a product object let's try to make one (hopefully they gave us the ID)
		if ( ! is_object( $product ) ) {
			$product = wc_get_product( $product );
		}

		// check if earned points are set at product-level
		$points = $this->pgs_woo_api_get_product_points( $product, $order );

		if ( is_numeric( $points ) ) {
			return $points;
		}

		// check if earned points are set at category-level
		$points = $this->pgs_woo_api_get_category_points( $product, $order );

		if ( is_numeric( $points ) ) {
			return $points;
		}

		// otherwise, show the default points set for the price of the product
        if(!class_exists('WC_Points_Rewards_Manager')){
			return 0;
        }
        return WC_Points_Rewards_Manager::calculate_points( $product->get_price() );
	}


    public function pgs_woo_api_get_product_points( $product, $order = null ) {
		$variation_id = ( version_compare( WC_VERSION, '3.0', '<' ) && isset( $product->variation_id ) ) ? $product->variation_id : $product->get_id();

		if ( empty( $variation_id ) ) {
			// simple or variable product, for variable product return the maximum possible points earned
			$points = get_post_meta( $product->get_id(), '_wc_points_max_discount', true );
			if ( ! method_exists( $product, 'get_variation_price' ) ) {
				// subscriptions integration - if subscriptions is active check if this is a renewal order
				if ( $this->pgs_woo_api_is_order_renewal( $order ) ) {
					$renewal_points = get_post_meta( $variation_id, '_wc_points_renewal_points', true );
					$points = ( $renewal_points ) ? $renewal_points : $points;
				}
			}
		} else {
			// variation product
			$points = get_post_meta( $variation_id, '_wc_points_earned', true );

			// subscriptions integration - if subscriptions is active check if this is a renewal order
			if ( $this->pgs_woo_api_is_order_renewal( $order ) ) {
				$renewal_points = get_post_meta( $variation_id, '_wc_points_renewal_points', true );
				$points = ( $renewal_points ) ? $renewal_points : $points;
			}

			// if points aren't set at variation level, use them if they're set at the product level
			if ( '' === $points ) {
				$points = get_post_meta( $product->get_id(), '_wc_points_earned', true );

				// subscriptions integration - if subscriptions is active check if this is a renewal order
				if ( $this->pgs_woo_api_is_order_renewal( $order ) ) {
					$renewal_points = get_post_meta( $product->get_id(), '_wc_points_renewal_points', true );
					$points = ( $renewal_points ) ? $renewal_points : $points;
				}
			}
		} // End if().

		// if a percentage modifier is set, adjust the points for the product by the percentage
		if ( false !== strpos( $points, '%' ) ) {
			$points =  $this->pgs_woo_api_calculate_points_multiplier( $points, $product );
		}

		return $points;
	}


    public function pgs_woo_api_get_category_points( $product, $order = null ) {
		global $wpdb;

		if ( $product->is_type( 'variation' ) ) {
			$product_id = version_compare( WC_VERSION, '3.0.0', '<' ) ? $product->parent_id : $product->get_parent_id();
		} else {
			$product_id = $product->get_id();
		}

		$category_ids = wc_get_product_terms( $product_id, 'product_cat', array( 'fields' => 'ids' ) );

		if ( ! $category_ids ) {
			return '';
		}

		$category_points = '';

		$category_ids_string = implode( ',', array_map( 'intval', $category_ids ) );

		if ( version_compare( WC()->version, '2.6.0', '>=' ) ) {
			$category_points_query = "SELECT term_id AS category_id, meta_value AS points FROM {$wpdb->termmeta} WHERE meta_key = '_wc_points_earned' AND term_id IN ( $category_ids_string );";
		} else {
			$category_points_query = "SELECT woocommerce_term_id AS category_id, meta_value AS points FROM {$wpdb->woocommerce_termmeta} WHERE meta_key = '_wc_points_earned' AND woocommerce_term_id IN ( $category_ids_string );";
		}

		$category_points_data = $wpdb->get_results( $category_points_query );

		$category_points_array = array();

		if ( $category_points_data && count( $category_points_data ) > 0 ) {
			foreach ( $category_points_data as $category ) {
				$category_points_array[ $category->category_id ] = $category->points;
			}
		}

		foreach ( $category_points_array as $category_id => $points ) {

			// subscriptions integration - if subscriptions is active check if this is a renewal order
			if ( $this->pgs_woo_api_is_order_renewal( $order ) ) {
				$renewal_points = get_term_meta( $category_id, '_wc_points_renewal_points', true );
				$points = ( $renewal_points ) ? $renewal_points : $points;
			}

			// if a percentage modifier is set, adjust the default points earned for the category by the percentage
			if ( false !== strpos( $points, '%' ) ) {
				$points = $this->pgs_woo_api_calculate_points_multiplier( $points, $product );
			}

			if ( ! is_numeric( $points ) ) {
				continue;
			}

			// in the case of a product being assigned to multiple categories with differing points earned, we want to return the biggest one
			if ( $points >= (int) $category_points ) {
				$category_points = $points;
			}
		}

		return $category_points;
	}

    protected function pgs_woo_api_is_order_renewal( $order ) {
		if ( ! function_exists( 'wcs_order_contains_resubscribe' ) || ! function_exists( 'wcs_order_contains_renewal' ) ) {
			return false;
		}

		if ( ! wcs_order_contains_resubscribe( $order ) && ! wcs_order_contains_renewal( $order ) ) {
			return false;
		}

		return true;
	}

    private function pgs_woo_api_calculate_points_multiplier( $percentage, $product ) {

		$percentage = str_replace( '%', '', $percentage ) / 100;

		return $percentage * WC_Points_Rewards_Manager::calculate_points( $product->get_price() );
	}


    private function pgs_woo_api_replace_message_variables( $message, $product ) {

		global $wc_points_rewards;

		$points_earned = $this->pgs_woo_api_get_points_earned_for_product_purchase( $product );

		// the min/max points earned for variable products can't be determined reliably, so the 'earn X points...' message
		// is not shown until a variation is selected, unless the prices for the variations are all the same
		// in which case, treat it like a simple product and show the message
		if ( method_exists( $product, 'get_variation_price' ) && $product->get_variation_price( 'min' ) != $product->get_variation_price( 'max' ) ) {
            return '';
		}

		// For BW compatibility, check to see if wc_min_points_earned exists, if not create it.
		if ( method_exists( $product, 'get_variation_price' ) ) {

			$wc_min_points_earned = get_post_meta( $product->get_id(), '_wc_min_points_earned', true );

			if ( ! $wc_min_points_earned ) {
				$wc_max_points_earned = '';
				$variable_points = array();

				if ( count( $product->get_children() ) > 0 ) {
					foreach ( $product->get_children() as $child ) {
						$earned = get_post_meta( $child, '_wc_points_earned', true );
						if ( '' !== $earned ) {
							$variable_points[] = $earned;
						}
					}
				}

				if ( count( $variable_points ) > 0 ) {
					$wc_min_points_earned = min( $variable_points );
				}

				update_post_meta( $product->get_id(), '_wc_min_points_earned', $wc_min_points_earned );
			}
		}

		$max_points_earned = get_post_meta( $product->get_id(), '_wc_points_max_discount', true );

		// Check to see if the minimum points earned is different from the max points earned, if so, dont show the message
		if ( method_exists( $product, 'get_variation_price' ) && isset( $wc_min_points_earned ) && $wc_min_points_earned != $max_points_earned ) {
			return '';
		}

		// Check to see if any max_points_earned is less than what the user would get with regular point applied for a variation
		if ( method_exists( $product, 'get_variation_price' ) ) {
			$variations = $product->get_available_variations();

			if ( $this->pgs_woo_api_get_highest_points_variation( $variations, $product->get_id() ) > $max_points_earned ) {
				return '';
			}
		}

		// points earned
		$message = str_replace( '{points}', number_format_i18n( $points_earned ), $message );

		// points label
		$message = str_replace( '{points_label}', $wc_points_rewards->get_points_label( $points_earned ), $message );

		if ( method_exists( $product, 'get_variation_price' ) ) {
			$message = '<span class="wc-points-rewards-product-variation-message">' . $message . '</span><br />';
		} else {
			$message = '<span class="wc-points-rewards-product-message">' . $message . '</span><br />';
		}
		return $message;
	}


    /**
	 * Get the variation with the highest points and return the points value
	 *
	 * @since 1.2.6
	 */
	public function pgs_woo_api_get_highest_points_variation( $variations, $product_id ) {

		// get transient name
		$transient_name = $this->pgs_woo_api_transient_highest_point_variation( $product_id );

		// see if we already have this data saved
		$points = get_transient( $transient_name );

		// if we don't have anything saved we'll have to figure it out
		if ( false === $points ) {
			// find the variation with the most points
			$highest = array( 'key' => 0, 'points' => 0 );
			foreach ( $variations as $key => $variation ) {
				// get points
				$points = $this->pgs_woo_api_get_points_earned_for_product_purchase( $variation['variation_id'] );
				// if this is the highest points value save it
				if ( $points > $highest['points'] ) {
					$highest = array( 'key' => $key, 'points' => $points );
				}
			}
			$points = $highest['points'];

			// save this for future use
			set_transient( $transient_name, $points, YEAR_IN_SECONDS );
		}

		return $points;
	}

    /**
	 * Get highest point variation transient name
	 *
	 * @since 1.2.6
	 */
	public function pgs_woo_api_transient_highest_point_variation( $product_id ) {
		return 'wc_points_rewards_highest_point_variation_' . $product_id;
	}
}