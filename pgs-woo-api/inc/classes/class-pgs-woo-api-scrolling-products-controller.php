<?php
class PGS_WOO_API_ScrollingController {

	private $is_currency_switcher_active = false;
	private $is_yith_featured_video_active = false;


	/**
	 * This function will retun all the sale products.
	 * when second time this API will call with param
	 * @params: product-per-page, on_sale(bool)
	 * it will return random product list.
	 */
	public function pgs_woo_api_get_scrolling_random_product($request){

        $per_page = 10;
        if(isset($request['product-per-page'])) {
    		$per_page = $request['product-per-page'];
		}

		$on_sale = true;
		if(isset($request['on_sale'])) {
    		$on_sale = $request['on_sale'];
		}

        $status = isset($request['status']) ? $request['status'] : 'publish';

        $args = array(
            'post_type' 			=> 'product',
    		'post_status' 			=> $status,
    		'ignore_sticky_posts'   => 1,
    		'posts_per_page'		=> $per_page,
            'tax_query'             => array()
        );

        if(isset($request['seller_id']) && !empty($request['seller_id'])){
            $args['author'] = $request['seller_id'];
        }

        $page = 1;
        if(isset($request['page'])) {
    		$page = $request['page'];
            $args['paged'] = $page;
    	}

		$category = isset($request['category']) ? $request['category'] : false;
        if(!empty($category)){
           // $terms = explode( ',', $category );
            $args['tax_query'] = array(
        		'relation' => 'AND',
        		array(
        			'taxonomy' => 'product_cat',
        			'field'    => 'slug',
        			'terms'    => $request['category'],
        			'operator' => 'IN',
        		),
        	);
        }
        $attributes = isset($request['attribute']) ? $request['attribute'] : false;
        if(!empty($attributes) || $attributes != null){
			$i=1;
			foreach($attributes as $attribute ){

				if(isset($attribute['name']) && !empty($attribute['name'])){
					$list[] = $i;
                    $terms = $attribute['options'];
					if(isset($terms) && !empty($terms)){
                        $attr_slug = $this->pgs_woo_api_get_attribute_taxonomie_slug_by_id($attribute['id']);
                        if($attr_slug != ''){
                            array_push($args['tax_query'],array(
    								'taxonomy' => $attr_slug,
    								'field' => 'name',
    								'terms' => $terms
    							)
    						);
                        }
                    }
                }
			    $i++;
			}
			if(!empty($list)){
				if(count($list) > 1){
					$args['tax_query']['relation'] = 'AND';
				}
			}
		}

		// Filter featured.
        if(isset($request['featured'])){

            if ( is_bool( $request['featured'] ) ) {
                $product_visibility_term_ids = wc_get_product_visibility_term_ids();
                if ( 'yes' === get_option( 'woocommerce_hide_out_of_stock_items' ) ) {
        			$args['tax_query'] = array(
        				array(
        					'taxonomy' => 'product_visibility',
        					'field'    => 'term_taxonomy_id',
        					'terms'    => $product_visibility_term_ids['outofstock'],
        					'operator' => 'NOT IN',
        				),
        			); // WPCS: slow query ok.
        		}

				$args['tax_query'][] = array(
					'taxonomy' => 'product_visibility',
					'field'    => 'term_taxonomy_id',
					'terms'    => $product_visibility_term_ids['featured'],
				);

            }
        }




        $search = isset($request['search']) ? $request['search'] : false;
        if(!empty($search) && $search != null){
			$args['s'] = $search;
		}



        $product_visibility_terms  = wc_get_product_visibility_term_ids();
		$product_visibility_not_in = array( is_search() && $main_query ? $product_visibility_terms['exclude-from-search'] : $product_visibility_terms['exclude-from-catalog'] );

		// Hide out of stock products.
		if ( 'yes' === get_option( 'woocommerce_hide_out_of_stock_items' ) ) {
			$product_visibility_not_in[] = $product_visibility_terms['outofstock'];
		}

        // Filter by rating.
		if ( isset( $request['rating_filter'] ) ) { // WPCS: input var ok, CSRF ok.
			$rating_filter = array_filter( array_map( 'absint', explode( ',', $request['rating_filter'] ) ) ); // WPCS: input var ok, CSRF ok, Sanitization ok.
			$rating_terms  = array();
			for ( $i = 1; $i <= 5; $i ++ ) {
				if ( in_array( $i, $rating_filter, true ) && isset( $product_visibility_terms[ 'rated-' . $i ] ) ) {
					$rating_terms[] = $product_visibility_terms[ 'rated-' . $i ];
				}
			}
			if ( ! empty( $rating_terms ) ) {
				$args['tax_query'][] = array(
					'taxonomy'      => 'product_visibility',
					'field'         => 'term_taxonomy_id',
					'terms'         => $rating_terms,
					'operator'      => 'IN',
					'rating_filter' => true,
				);
			}
		}
        if ( !empty( $args['tax_query'] ) ) {
			$args['tax_query']['relation'] = 'AND';
		}
        if ( ! empty( $product_visibility_not_in ) ) {
			$args['tax_query'][] = array(
				'taxonomy' => 'product_visibility',
				'field'    => 'term_taxonomy_id',
				'terms'    => $product_visibility_not_in,
				'operator' => 'NOT IN',
			);
		}

		// Filter by tax class.
        if(isset($request['tax_class'])){
    		if ( ! empty( $request['tax_class'] ) ) {
    			$args['meta_query'] = $this->add_meta_query( $args, array(
    				'key'   => '_tax_class',
    				'value' => 'standard' !== $request['tax_class'] ? $request['tax_class'] : '',
    			) );
    		}
        }

		// Price filter.
		if(isset($request['min_price']) || isset($request['max_price']) ){
            if ( ! empty( $request['min_price'] ) || ! empty( $request['max_price'] ) ) {
    			$args['meta_query'] = $this->add_meta_query( $args, wc_get_min_max_price_meta_query( $request ) );
    		}
        }

		// Filter product in stock or out of stock.
		if(isset($request['in_stock'])){
            if ( is_bool( $request['in_stock'] ) ) {
    			$args['meta_query'] = $this->add_meta_query( $args, array(
    				'key'   => '_stock_status',
    				'value' => true === $request['in_stock'] ? 'instock' : 'outofstock',
    			) );
    		}
        }

		// Filter by on sale products.
		/*if(isset($on_sale)){

            if ( is_bool( $on_sale ) ) {

                $args['meta_query'] = array(
                    'relation' => 'OR',
                    array( // Simple products type
                        'key'           => '_sale_price',
                        'value'         => 0,
                        'compare'       => '>',
                        'type'          => 'numeric'
                    ),
                    array( // Variable products type
                        'key'           => '_min_variation_sale_price',
                        'value'         => 0,
                        'compare'       => '>',
                        'type'          => 'numeric'
					),
					array(
						'key' => '_sale_price_dates_to',
						'value'   => '',
						'compare' => 'NOT IN'
					)
                );
    		}
        }*/

        // Get product with ip or ips array
        $include = isset($request['include']) ? $request['include'] : false;
        if(!empty($include)){
			$in = explode(",",$include);
			$args['post__in'] = $in;
		}

        if(empty($args['tax_query'])){
            unset($args['tax_query']);
        }

		/*$args['meta_query']=array(
				array(
					'key' => '_sale_price_dates_to',
					'value'   => '',
					'compare' => 'IN'
				)
			);*/
		/*print_r($args);
		die;*/
		$args['orderby'] = 'rand';
		$args['order'] = 'desc';
		/*if ( !is_bool( $on_sale ) ) {
			$loop = new WP_Query( $args );
			$post_ids = wp_list_pluck( $loop->posts, 'ID' );
			wp_reset_postdata();

			$args['orderby'] = 'rand';
			$args['order'] = 'desc';
		}*/ /*else{
			$args['orderby'] = '_sale_price_dates_to';
			$args['order'] = 'ASC';
		}*/



		//set the random order


		$args['posts_per_page']=$per_page;
		$args['paged'] = 1;
		//new loop
		//check if loaded is there
		$args['post__not_in']=array();
		if(!empty($request['loaded'])){
			$args['post__not_in']=$request['loaded'];
		}
		if(isset($post_ids) && !empty($post_ids)){
			$args['post__not_in'] = array_merge($args['post__not_in'],$post_ids);
		}

		if ( !is_bool( $on_sale ) ) {
			unset($args['meta_query']);
		}
		/*echo '<pre>';
		print_r($args);
		echo '</pre>';*/
		$loop = new WP_Query( $args );
		/*echo '<pre>';
		print_r($loop->request);
		echo '</pre>';*/
		$error['status'] = 'error';

		$pgs_woo_api = new PGS_WOO_API_Controller;

        if($loop->have_posts()):
			$this->is_yith_featured_video_active =pgs_woo_api_is_yith_featured_video_active();
			while ( $loop->have_posts() ) : $loop->the_post();
                $product_id = $loop->post->ID;
                $product_data = $this->get_products_data($product_id);

                $seller_info = $pgs_woo_api->pgs_woo_api_get_seller_short_details($product_id);
                $product_data['seller_info'] = $seller_info;

                $data[] = $product_data;
             endwhile;
            wp_reset_postdata();
		else :
            $error['message'] = esc_html__("No product found","pgs-woo-api");
            return $error;
        endif;
        return $data;
	}

	public function pgs_woo_api_get_attribute_taxonomie_slug_by_id($id){
        $attribute_taxonomie_slug = '';
    	if(isset($id) && $id > 0){
            global $wpdb;
            $attribute_taxonomie = $wpdb->get_row( "SELECT * FROM " . $wpdb->prefix . "woocommerce_attribute_taxonomies WHERE attribute_id = $id" );
            if(isset($attribute_taxonomie) && !empty($attribute_taxonomie)){
                $attr_name = $attribute_taxonomie->attribute_name;
                $attribute_taxonomie_slug = 'pa_'.$attr_name;
            }
        }
        return $attribute_taxonomie_slug;
    }


    /**
	 * Add meta query.
	 *
	 */
	public function add_meta_query( $args, $meta_query ) {
		if ( ! empty( $args['meta_query'] ) ) {
			$args['meta_query'] = array();
		}

		$args['meta_query'][] = $meta_query;

		return $args['meta_query'];
	}



    public function get_products_data($product_id){
        pgs_woo_api_hook_remove_tax_in_price_html();//Remove include tax in price html

        $wcp = wc_get_product($product_id);
        $wce = new WC_Product_External($product_id);

        $rewards_message = '';
        $is_reward_points_active = pgs_woo_api_is_reward_points_active();
        if($is_reward_points_active){
            $rewards_Product = new PGS_WOO_API_RewardsController();
            $rewards_msg = $rewards_Product->get_single_product_rewards_message($wcp);
            if(isset($rewards_msg) && !empty($rewards_msg)){
                $rewards_message = $rewards_msg;
            }
        }

        $get_price = $wcp->get_price();
        $regular_price = $wcp->get_regular_price();
        $sale_price = $wcp->get_sale_price();
        $wc_tax_enabled = wc_tax_enabled();
        $tax_status =  'none';
        $tax_class = '';
        if($wc_tax_enabled){
            $tax_price = wc_get_price_to_display( $wcp );	//tax
            $price_including_tax = wc_get_price_including_tax( $wcp );
            $price_excluding_tax = wc_get_price_excluding_tax( $wcp );
            $tax_status =  $wcp->get_tax_status();
            $tax_class = $wcp->get_tax_class();
        }
        if($this->is_currency_switcher_active){
			$pgs_woo_api = new PGS_WOO_API_Controller;
            $regular_price = $pgs_woo_api->pgs_woo_api_update_currency_rate($regular_price);
            $sale_price = $pgs_woo_api->pgs_woo_api_update_currency_rate($sale_price);
            $get_price = $pgs_woo_api->pgs_woo_api_update_currency_rate($get_price);
            if($wc_tax_enabled){
                $tax_price = $pgs_woo_api->pgs_woo_api_update_currency_rate($tax_price);
                $price_including_tax = $pgs_woo_api->pgs_woo_api_update_currency_rate($price_including_tax);
                $price_excluding_tax = $pgs_woo_api->pgs_woo_api_update_currency_rate($price_excluding_tax);
            }
        }
        $addition_info_html = '';
        $addition_info_data = array_filter( $wcp->get_attributes(), 'wc_attributes_array_filter_visible' );
        if ( $wcp && ( $wcp->has_attributes() || apply_filters( 'wc_product_enable_dimensions_display', $wcp->has_weight() || $wcp->has_dimensions() ) ) ) {
            $addition_info_html = $this->pgs_woo_api_get_addition_info_data($addition_info_data,$wcp);
        }

        $tax_price = (isset($tax_price))?$tax_price:'';
        $price_including_tax = (isset($price_including_tax))?$price_including_tax:'';
		$price_excluding_tax = (isset($price_excluding_tax))?$price_excluding_tax:'';

		$featured_video = (object)array();
        if($this->is_yith_featured_video_active){
			$pgs_woo_api = new PGS_WOO_API_Controller;
            $featured_video = $this->pgs_woo_api_get_yith_featured_video($wcp,$product_id);
        }
        $data = array(
			'id' => $wcp->get_id(),
			'name' => $wcp->get_name(),
			'slug' => $wcp->get_slug(),
			'permalink' =>  $wcp->get_permalink(),
			'date_created' => wc_rest_prepare_date_response( $wcp->get_date_created(), false ),
			'date_created_gmt' => wc_rest_prepare_date_response( $wcp->get_date_created() ),
			'date_modified' =>wc_rest_prepare_date_response( $wcp->get_date_modified(), false ),
			'date_modified_gmt' => wc_rest_prepare_date_response( $wcp->get_date_modified() ),
			'type' => $wcp->get_type(),
			'status' => $wcp->get_status(),
			'featured' => $wcp->get_featured(),
			'catalog_visibility' => $wcp->get_catalog_visibility(),
			'description' => $wcp->get_description(),
			'short_description' => $wcp->get_short_description(),
			'sku' =>  $wcp->get_sku(),
			'price' =>  $get_price,
            'tax_price'=> $tax_price, //tax
            'price_excluding_tax' => $price_excluding_tax,
            'price_including_tax' => $price_including_tax,
			'regular_price' => $regular_price,
			'sale_price' => $sale_price,
			'date_on_sale_from' => wc_rest_prepare_date_response($wcp->get_date_on_sale_from()),
			'date_on_sale_from_gmt' => wc_rest_prepare_date_response($wcp->get_date_on_sale_from()),
			'date_on_sale_to' =>  wc_rest_prepare_date_response($wcp->get_date_on_sale_to()),
			'date_on_sale_to_gmt' =>  wc_rest_prepare_date_response($wcp->get_date_on_sale_to()),
			'price_html' => $wcp->get_price_html(),
			'on_sale' => $wcp->is_on_sale(),
			'purchasable' => $wcp->is_purchasable(),
			'total_sales' => $wcp->get_total_sales(),
			'virtual' => $wcp->get_virtual(),
			'downloadable' => $wcp->get_downloadable(),
			'downloads' => $wcp->get_downloads(),
			'download_limit' => $wcp->get_download_limit(),
			'download_expiry' => $wcp->get_download_expiry(),
			'external_url' => $wce->get_product_url(),
			'button_text' => $wce->get_button_text(),
			'tax_status' =>  $tax_status,
            'tax_class' => $tax_class,
			'manage_stock' => $wcp->get_manage_stock(),
			'stock_quantity' => $wcp->get_stock_quantity(),
			'in_stock' => $wcp->is_in_stock(),
			'backorders' => $wcp->get_backorders(),
			'backorders_allowed' => $wcp->backorders_allowed(),
			'backordered' => $wcp->backorders_allowed(),
			'sold_individually' => $wcp->get_sold_individually(),
			'weight' => $wcp->get_weight(),
			'dimensions'            => array(
				'length' => $wcp->get_length(),
				'width'  => $wcp->get_width(),
				'height' => $wcp->get_height(),
			),
			'shipping_required'     => $wcp->needs_shipping(),
			'shipping_taxable'      => $wcp->is_shipping_taxable(),
			'shipping_class'        => $wcp->get_shipping_class(),
			'shipping_class_id'     => $wcp->get_shipping_class_id(),
			'reviews_allowed'       => $wcp->get_reviews_allowed(),
			'average_rating'        => $wcp->get_average_rating(),
			'rating_count'          => $wcp->get_review_count(),
			'related_ids'           => array_map( 'absint', array_values( wc_get_related_products( $wcp->get_id() ) ) ),
			'upsell_ids'            => array_map( 'absint', $wcp->get_upsell_ids() ),
			'cross_sell_ids'        => array_map( 'absint', $wcp->get_cross_sell_ids() ),
			'parent_id'             => $wcp->get_parent_id(),
			'purchase_note'         => wpautop( do_shortcode( wp_kses_post( $wcp->get_purchase_note() ) ) ),
			'categories'            => $this->get_taxonomy_terms( $wcp ),
			'tags'                  => $this->get_taxonomy_terms( $wcp, 'tag' ),
			'images'                => $this->get_images( $wcp ),
			'app_thumbnail'         => $this->get_app_thumbnail($wcp),
            'attributes'            => $this->get_attributes( $wcp ),
			'default_attributes'    => $this->get_default_attributes( $wcp ),
			'variations'            => array(),
			'grouped_products'      => array(),
			'menu_order'            => $wcp->get_menu_order(),
			'meta_data'             => $wcp->get_meta_data(),
            'rewards_message'       => $rewards_message,
            'addition_info_html'    => (isset($addition_info_html) && !empty($addition_info_html))?$addition_info_html:'',
			'featured_video'        => $featured_video
		);

        // Add variations to variable products.
		if ( $wcp->is_type( 'variable' ) && $wcp->has_child() ) {
			$data['variations'] = $wcp->get_children();
		}

        // Add grouped products data.
		if ( $wcp->is_type( 'grouped' ) && $wcp->has_child() ) {
			$data['grouped_products'] = $wcp->get_children();
		}
        return $data;
    }

    public function get_average_rating($idproduct){
		$argscomment = array(
			'post_id' => $idproduct,
			'status' => 'approve',
			'offset' => 0
		);
		$comments = get_comments( $argscomment );
		if(!empty($comments))
		{
			$numcomment = 0;
			$ratings = 0;
			foreach($comments as $comment) {
                $cmentId = $comment->comment_ID;
				$rating = get_comment_meta( $cmentId, 'rating', true );
				if(isset($rating) && !empty($rating)){
				    $ratings += $rating;
				}
				$numcomment++;
			}
			$avgrating = floatval($ratings / $numcomment);
		}
		else{
			$avgrating = "";
		}
		return $avgrating;
	}

    /**
	 * Get taxonomy terms.
	 *
	 * @param WC_Product $product  Product instance.
	 * @param string     $taxonomy Taxonomy slug.
	 * @return array
	 */
	public function get_taxonomy_terms( $product, $taxonomy = 'cat' ) {
		$terms = array();

		foreach ( wc_get_object_terms( $product->get_id(), 'product_' . $taxonomy ) as $term ) {
			$terms[] = array(
				'id'   => $term->term_id,
				'name' => $term->name,
				'slug' => $term->slug,
			);
		}

		return $terms;
	}

    /**
	 * Get the images for a product or product variation.
	 *
	 * @param WC_Product|WC_Product_Variation $product Product instance.
	 * @return array
	 */
	public function get_images( $product ) {
		$images = array();
		$attachment_ids = array();

		// Add featured image.
		if ( has_post_thumbnail( $product->get_id() ) ) {
			$attachment_ids[] = $product->get_image_id();
		}

		// Add gallery images.
		$attachment_ids = array_merge( $attachment_ids, $product->get_gallery_image_ids() );

		// Build image data.
		foreach ( $attachment_ids as $position => $attachment_id ) {
			$attachment_post = get_post( $attachment_id );
			if ( is_null( $attachment_post ) ) {
				continue;
			}

			$attachment = wp_get_attachment_image_src( $attachment_id, apply_filters( 'pgs_woo_api_single_product_image', 'large' ) );
			if ( ! is_array( $attachment ) ) {
				continue;
			}


			$images[] = array(
				'id'                => (int) $attachment_id,
				'date_created'      => wc_rest_prepare_date_response( $attachment_post->post_date, false ),
				'date_created_gmt'  => wc_rest_prepare_date_response( strtotime( $attachment_post->post_date_gmt ) ),
				'date_modified'     => wc_rest_prepare_date_response( $attachment_post->post_modified, false ),
				'date_modified_gmt' => wc_rest_prepare_date_response( strtotime( $attachment_post->post_modified_gmt ) ),
				'src'               => current( $attachment ),
				'name'              => get_the_title( $attachment_id ),
				'alt'               => get_post_meta( $attachment_id, '_wp_attachment_image_alt', true ),
				'position'          => (int) $position,
			);
		}

		// Set a placeholder image if the product has no images set.
		if ( empty( $images ) ) {
			$images[] = array(
				'id'                => 0,
				'date_created'      => wc_rest_prepare_date_response( current_time( 'mysql' ), false ), // Default to now.
				'date_created_gmt'  => wc_rest_prepare_date_response( current_time( 'timestamp', true ) ), // Default to now.
				'date_modified'     => wc_rest_prepare_date_response( current_time( 'mysql' ), false ),
				'date_modified_gmt' => wc_rest_prepare_date_response( current_time( 'timestamp', true ) ),
				'src'               => wc_placeholder_img_src(),
				'name'              => __( 'Placeholder', 'pgs-woo-api' ),
				'alt'               => __( 'Placeholder', 'pgs-woo-api' ),
				'position'          => 0,
			);
		}

		return $images;
	}

    /**
	 * Get the app thumbnail for single image in list
	 *
	 */
	public function get_app_thumbnail( $product ) {
		$images = array();$images_url='';
		$attachment_ids = array();

		// Add featured image.
		if ( has_post_thumbnail( $product->get_id() ) ) {
			$attachment_id = $product->get_image_id();
            $images = wp_get_attachment_image_src( $attachment_id, 'app_thumbnail' );
            $images_url = $images[0];
        } else {
            $attachment_ids = $product->get_gallery_image_ids();
    		// Build image data.
    		foreach ( $attachment_ids as $position => $attachment_id ) {
    			$attachment_post = get_post( $attachment_id );
    			if ( is_null( $attachment_post ) ) {
    				continue;
    			}

    			$attachment = wp_get_attachment_image_src( $attachment_id, 'app_thumbnail' );
    			if(!empty($attachment)){
                    $images_url = current( $attachment );
                    break;
    			}
    		}
        }
        if(empty($images_url)){
            $images_url = wc_placeholder_img_src();
        }
		return $images_url;
	}


    /**
	 * Get the attributes for a product or product variation.
	 * @return array
	 */
	public function get_attributes( $product ) {
		$attributes = array();

		if ( $product->is_type( 'variation' ) ) {
			$_product = wc_get_product( $product->get_parent_id() );
			foreach ( $product->get_variation_attributes() as $attribute_name => $attribute ) {
				$name = str_replace( 'attribute_', '', $attribute_name );

				if ( ! $attribute ) {
					continue;
				}

				// Taxonomy-based attributes are prefixed with `pa_`, otherwise simply `attribute_`.
				if ( 0 === strpos( $attribute_name, 'attribute_pa_' ) ) {
					$option_term = get_term_by( 'slug', $attribute, $name );
					$attributes[] = array(
						'id'     => wc_attribute_taxonomy_id_by_name( $name ),
						'name'   => $this->get_attribute_taxonomy_name( $name, $_product ),
						'option' => $option_term && ! is_wp_error( $option_term ) ? $option_term->name : $attribute,
					);
				} else {
					$attributes[] = array(
						'id'     => 0,
						'name'   => $this->get_attribute_taxonomy_name( $name, $_product ),
						'option' => $attribute,
					);
				}
			}
		} else {
			foreach ( $product->get_attributes() as $attribute ) {
				$attributes[] = array(
					'id'        => $attribute['is_taxonomy'] ? wc_attribute_taxonomy_id_by_name( $attribute['name'] ) : 0,
					'name'      => $this->get_attribute_taxonomy_name( $attribute['name'], $product ),
					'position'  => (int) $attribute['position'],
					'visible'   => (bool) $attribute['is_visible'],
					'variation' => (bool) $attribute['is_variation'],
					'options'   => $this->get_attribute_options( $product->get_id(), $attribute ),
					'new_options'   => $this->get_new_attribute_options( $product->get_id(), $attribute ),
				);
			}
		}

		return $attributes;
	}

    /**
	 * Get attribute options.
	 *
	 * @param int   $product_id Product ID.
	 * @param array $attribute  Attribute data.
	 * @return array
	 */
	public function get_attribute_options( $product_id, $attribute ) {


        if ( isset( $attribute['is_taxonomy'] ) && $attribute['is_taxonomy'] ) {
            return wc_get_product_terms( $product_id, $attribute['name'], array( 'fields' => 'names' ) );
		} elseif ( isset( $attribute['value'] ) ) {
            return array_map( 'trim', explode( '|', $attribute['value'] ) );
		}

		return array();
	}

	/**
	 * Get New attribute options.
	 *
	 * @param int   $product_id Product ID.
	 * @param array $attribute  Attribute data.
	 * @return array
	 */
	public function get_new_attribute_options( $product_id, $attribute ) {


        if ( isset( $attribute['is_taxonomy'] ) && $attribute['is_taxonomy'] ) {
			$value=wc_get_product_terms( $product_id, $attribute['name'], array( 'fields' => 'all' ) );
			$new_option=array();
			foreach($value as $val)
			{
				$image_preview = get_term_meta( $val->term_id, 'image_preview', true );
				$color_preview = get_term_meta( $val->term_id, 'color_preview', true );
				if( !empty($image_preview) ){
					$img_url = wp_get_attachment_image_src( $image_preview, 'ciyashop-brand-logo' );
				/*	$new_option[]=array(
									'name' => $val->name,
									'color'=>'',
									'image'=>$img_url[0]
								); */
				}
				else
				{
					$img_url="";
				}
				$new_option[]=array(
								'variation_name' => $val->name,
								'color'=>$color_preview,
								'image'=>$img_url[0]
							);


			}
			return $new_option;

		} elseif ( isset( $attribute['value'] ) ) {			$attributes = array_map( 'trim', explode( '|', $attribute['value'] ) );			foreach($attributes as $att){				$new_option[]=array(								'variation_name' => $att,								'color'			 => "",								'image'          => ""				);			}			return $new_option;
            //return array_map( 'trim', explode( '|', $attribute['value'] ) );
		}

		return array();
	}

	/**
	 * Get default attributes.
	 *
	 * @param WC_Product $product Product instance.
	 * @return array
	 */
	public function get_default_attributes( $product ) {
		$default = array();

		if ( $product->is_type( 'variable' ) ) {
			foreach ( array_filter( (array) $product->get_default_attributes(), 'strlen' ) as $key => $value ) {
				if ( 0 === strpos( $key, 'pa_' ) ) {
					$default[] = array(
						'id'     => wc_attribute_taxonomy_id_by_name( $key ),
						'name'   => $this->get_attribute_taxonomy_name( $key, $product ),
						'option' => $value,
					);
				} else {
					$default[] = array(
						'id'     => 0,
						'name'   => $this->get_attribute_taxonomy_name( $key, $product ),
						'option' => $value,
					);
				}
			}
		}

		return $default;
	}
	/**
	 * Get product attribute taxonomy name.
	 *
	 * @param  string     $slug    Taxonomy name.
	 * @param  WC_Product $product Product data.
	 * @return string
	 */
	public function get_attribute_taxonomy_name( $slug, $product ) {
		$attributes = $product->get_attributes();

		if ( ! isset( $attributes[ $slug ] ) ) {
			return str_replace( 'pa_', '', $slug );
		}

		$attribute = $attributes[ $slug ];

		// Taxonomy attribute name.
		if ( $attribute->is_taxonomy() ) {
			$taxonomy = $attribute->get_taxonomy_object();
			return $taxonomy->attribute_label;
		}

		// Custom product attribute name.
		return $attribute->get_name();
	}


    public function pgs_woo_api_set_tex_query_array($taxonomys,$post){
        $mileage_terms = array();
    	$arg = array();
        foreach($taxonomys as $tax){
            if(isset($post[$tax]) &&  $post[$tax] != ''){
    	       foreach($post as $key => $val){
    	           if($key == $tax){
                        $arg[] = array(
                			'taxonomy' => $tax,
                			'field'    => 'slug',
                			'terms'    => array($post[$tax]),
                		);
    	           }

    	       }
            }
        }
        return $arg;
    }


    public function pgs_woo_api_get_price_filter() {
    	global $wpdb;
    	$args       = $wp_the_query->query_vars;
    	$tax_query  = isset( $args['tax_query'] ) ? $args['tax_query'] : array();
    	$meta_query = isset( $args['meta_query'] ) ? $args['meta_query'] : array();

    	if ( ! empty( $args['taxonomy'] ) && ! empty( $args['term'] ) ) {
    		$tax_query[] = array(
    			'taxonomy' => $args['taxonomy'],
    			'terms'    => array( $args['term'] ),
    			'field'    => 'slug',
    		);
    	}

    	foreach ( $meta_query as $key => $query ) {
    		if ( ! empty( $query['price_filter'] ) || ! empty( $query['rating_filter'] ) ) {
    			unset( $meta_query[ $key ] );
    		}
    	}

    	$meta_query = new WP_Meta_Query( $meta_query );
    	$tax_query  = new WP_Tax_Query( $tax_query );

    	$meta_query_sql = $meta_query->get_sql( 'post', $wpdb->posts, 'ID' );
    	$tax_query_sql  = $tax_query->get_sql( $wpdb->posts, 'ID' );

        // Current site prefix
        $tbprefix = $wpdb->prefix;
        $sql   = "SELECT ";
        $sql  .= " min( FLOOR( price_meta.meta_value ) ) as min_price,";
        $sql  .= " max( CEILING( price_meta.meta_value ) ) as max_price";
        $sql  .= " FROM ".$tbprefix."posts";
        $sql  .= " LEFT JOIN ".$tbprefix."postmeta as price_meta ON ".$tbprefix."posts.ID = price_meta.post_id";
        $sql  .= " INNER JOIN ".$tbprefix."postmeta ON (".$tbprefix."posts.ID = ".$tbprefix."postmeta.post_id )";
        $sql  .= " WHERE ".$tbprefix."posts.post_type IN ('cars')";
        $sql  .= " AND ".$tbprefix."posts.post_status = 'publish'";
        $sql  .= " AND price_meta.meta_key IN ('sale_price','regular_price')";
        return $wpdb->get_row( $sql );

    }

    public function pgs_woo_api_get_addition_info_data($attributes,$product){
        $display_dimensions = apply_filters( 'pgs_woo_api_wc_product_enable_dimensions_display', $product->has_weight() || $product->has_dimensions() );
        $html = '';
        $html .= '<table class="shop_attributes">';
    	if ( $display_dimensions && $product->has_weight() ) :
    		$html .= '<tr>';
    			$html .= '<th>'.esc_html__( 'Weight', 'pgs-woo-api' ).'</th>';
    			$html .= '<td class="product_weight">'.esc_html( wc_format_weight( $product->get_weight() ) ).'</td>';
    		$html .= '</tr>';
    	endif;

    	if ( $display_dimensions && $product->has_dimensions() ) :
    		$html .= '<tr>';
    			$html .= '<th>'.esc_html__( 'Dimensions', 'pgs-woo-api' ).'</th>';
    			$html .= '<td class="product_dimensions">'.esc_html( wc_format_dimensions( $product->get_dimensions( false ) ) ).'</td>';
    		$html .= '</tr>';
    	endif;

        foreach ( $attributes as $attribute ) :
    		$html .= '<tr>';
    			$html .= '<th>'.wc_attribute_label( $attribute->get_name() ).'</th>';
    			$html .= '<td>';
    				$values = array();

    				if ( $attribute->is_taxonomy() ) {
    					$attribute_taxonomy = $attribute->get_taxonomy_object();
    					$attribute_values = wc_get_product_terms( $product->get_id(), $attribute->get_name(), array( 'fields' => 'all' ) );

    					foreach ( $attribute_values as $attribute_value ) {
    						$value_name = esc_html( $attribute_value->name );

    						if ( $attribute_taxonomy->attribute_public ) {
    							$values[] = $value_name;
    						} else {
    							$values[] = $value_name;
    						}
    					}
    				} else {
    					$values = $attribute->get_options();

    					foreach ( $values as &$value ) {
    						$value = esc_html( $value );
    					}
    				}

    				$html .= wptexturize( implode( ', ', $values ) );
    			$html .= '</td>';
    		$html .= '</tr>';
	   endforeach;
       $html .= '</table>';
       return $html;
    }
	public function pgs_woo_api_get_yith_featured_video($product,$product_id){
        $args = array();
        $video_url = yit_get_prop( $product, '_video_url' );
		if ( ! empty( $video_url ) ) {

			$result             = true;
			$video_host         = parse_url( esc_url( $video_url ) );
			$args['url']        = $video_url;
			$args['product_id'] = $product_id;
            //$YITH_Featured_Audio_Video = YITH_WC_Audio_Video::get_instance();
			//$src =  $atts['url'];
            $http = is_ssl() ? 'https' : 'http';
            list( $video_type, $video_id ) = explode( ':', ywcfav_video_type_by_url( $video_url ) );
            $args['video_type'] = $video_type;
            $args['video_id'] = $video_id;
            //echo $video_host['host'];
            /*global $YITH_Featured_Audio_Video;
            echo '<pre>';
            print_r($YITH_Featured_Audio_Video->hosts['youtube']);
            echo '</pre>';*/

            $image_url = '';
            //$image_url = $this->pgs_woo_api_get_yith_video_image_thmb($video_id,'youtube');
            if ( $video_type == 'youtube') {
                //$embed_url = $http.'://www.youtube.com/embed/'.$video_id.'?enablejsapi=1';
                $image_url = $this->pgs_woo_api_get_yith_video_image_thmb($video_id,'youtube');
			} elseif ( $video_type == 'vimeo' ) {
                //$embed_url = $http.'://www.youtube.com/embed/'.$video_id.'?enablejsapi=1';
                //$video_meta_id = "video_".$product_id;
                //$embed_url =  $http.'://player.vimeo.com/video/'.$video_id.'?api=1&player_id='.$video_meta_id;
                $image_url = $this->pgs_woo_api_get_yith_video_image_thmb($video_id,'vimeo');
			}
            //$args['embed_url'] = $embed_url;
            $args['image_url'] = $image_url;
        } else {
            $args = (object)array();
        }
        return $args;
    }
	public function pgs_woo_api_get_yith_video_image_thmb($video_id='',$host) {
		$result  = false;
		$img_url = '';
		if ( !empty($video_id) && !empty($host) ) {
			//$video_id = $video_info[0];
			//$host     = $video_info[1];
			switch ( $host ) {

				case 'vimeo' :
					$img_url = 'http://vimeo.com/api/v2/video/' . $video_id . '.xml';
					$xml     = simplexml_load_file( $img_url );
					$img_url = (string) $xml->video->thumbnail_large;
					$tmp     = getimagesize( $img_url );

					$result = ! is_wp_error( $tmp );

					break;
				case 'youtube':
					$img_url      = 'https://img.youtube.com/vi/' . $video_id . '/maxresdefault.jpg';
					$get_response = wp_remote_get( $img_url );
					$result       = ! is_wp_error( $get_response ) && $get_response['response']['code'] == '200';

					break;
			}
		}
		if ( ! $result ) {
			$img_url = YWCFAV_ASSETS_URL . '/images/videoplaceholder.jpg';
		}
		return $img_url;
	}
}
