<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_AttributesController extends  PGS_WOO_API_Controller{
	/**
	 * Endpoint namespace.
	 *
	 */
	protected $namespace = 'pgs-woo-api/v1';

	/**
	 * Route base.
	 */
	protected $rest_base = 'attributes';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {
		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}

	public function pgs_woo_api_register_route() {
        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_get_attributes'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/attributes
    * @param category slug: ####
    */
    public function pgs_woo_api_get_attributes(){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $args = array(
            'post_type' 			=> 'product',
    		'post_status' 			=> 'publish',
    		'ignore_sticky_posts'   => 1,
    		'posts_per_page'		=> -1
        );
        $error = array( "status" => "error" );
		$category = isset($request['category']) ? $request['category'] : false;
        if(!empty($category)){
            $terms = explode( ',', $category );
            $args['tax_query'] = array(
        		array(
        			'taxonomy' => 'product_cat',
        			'field'    => 'term_id',
        			'terms'    => $terms,
        			'operator' => 'IN',
        		),
        	);
        }

        $attributes = isset($request['attribute']) ? $request['attribute'] : false;
        if(!empty($attributes) || $attributes != null){
			$i=1;
			foreach($attributes as $attribute ){

				if(isset($attribute['slug']) && !empty($attribute['slug'])){
					$list[] = $i;
                    $terms = $attribute['options'];
					if(isset($terms) && !empty($terms)){
                        array_push($args['tax_query'],array(
								'taxonomy' => $attribute['slug'],
								'field' => 'name',
								'terms' => $terms
							)
						);
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
    			$args['tax_query'][] = array(
    				'taxonomy' => 'product_visibility',
    				'field'    => 'name',
    				'terms'    => 'featured',
    			);
    		}
        }

		// Filter by sku.
        if(isset($request['sku'])){
    		if ( ! empty( $request['sku'] ) ) {
    			$skus = explode( ',', $request['sku'] );
    			// Include the current string as a SKU too.
    			if ( 1 < count( $skus ) ) {
    				$skus[] = $request['sku'];
    			}

    			$args['meta_query'] = $this->add_meta_query( $args, array(
    				'key'     => '_sku',
    				'value'   => $skus,
    				'compare' => 'IN',
    			) );
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
		if(isset($request['on_sale'])){
            if ( is_bool( $request['on_sale'] ) ) {
    			$on_sale_key           = $request['on_sale'] ? 'post__in' : 'post__not_in';
    			$args[ $on_sale_key ] += wc_get_product_ids_on_sale();
    		}
        }

		// Force the post_type argument, since it's not a user input variable.
		if(isset($request['sku'])){
            if ( ! empty( $request['sku'] ) ) {
    			$args['post_type'] = array( 'product', 'product_variation' );
    		} else {
    			$args['post_type'] = $this->post_type;
    		}
        }

        $include = isset($request['include']) ? $request['include'] : false;
        if(!empty($include)){
			$in = explode(",",$include);
			$args['post__in'] = $in;
		}


        if(isset($request['order_by'])){
            $order_by = $request['order_by'];

            switch ($order_by){
                case 'price':
                    $args['orderby'] = 'meta_value_num';
                    $args['meta_key'] = '_price';
                    $args['order'] = 'ASC';
                    break;

                case 'price-desc':
                    $args['orderby'] = 'meta_value_num';
                    $args['meta_key'] = '_price';
                    $args['order'] = 'DESC';
                    break;

                case 'rating':
                    //$args['orderby'] = 'meta_value_num';
                    $args['meta_key'] = '_wc_average_rating';
                    //$args['order'] = 'desc';
                    $args['orderby']  = array(
    					'meta_value_num' => 'DESC',
    					'ID' => 'ASC',
    				);
                    break;

                case 'popularity':
                    $args['orderby'] = 'meta_value_num';
                    $args['meta_key'] = 'total_sales';
                    $args['order'] = 'DESC';
                    break;
            }
        }
        $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');

        $loop = new WP_Query( $args );
        $attributes = array();
        $temp_arr = array();
        if($loop->have_posts()):
            while ( $loop->have_posts() ) : $loop->the_post();
                $wcp = wc_get_product($loop->post->ID);
                $obj = $this->pgs_get_attributes( $wcp );
                if(isset($obj) && !empty($obj)){
                    foreach($obj as $items){

                        $attribute    = wc_sanitize_taxonomy_name( $items['name'] );
                        $taxonomy     = wc_attribute_taxonomy_name( $attribute );
                        $is_filters = (isset($pgs_woo_api_home_option['pgs_api_filters'][$taxonomy]))?$pgs_woo_api_home_option['pgs_api_filters'][$taxonomy]:'enable';
                        if($is_filters == 'enable'){
                            if(!in_array($items['id'],$temp_arr)){
                                $attributes[] = $items;
                                $temp_arr[] = $items['id'];
                            } else {
                                foreach($attributes as $key => $atr){
                                    if( (isset($atr['id']) && isset($items['id']) ) && ($atr['id'] == $items['id']) && ($items['name'] != 'color')){
                                        $result = array_merge_recursive($atr['options'],$items['options']);
                                        $result = array_unique($result);
                                        sort($result);
                                        $attributes[$key]['options'] = $result;
                                    } elseif( (isset($atr['id']) && isset($items['id'])) && ($atr['id'] == $items['id']) && ($items['name'] == 'color') ){
                                        $color_array = array_merge_recursive($atr['options'],$items['options']);
                                        $color_result = $this->pgs_woo_api_set_unique_associate_array($color_array);
                                        sort($color_result);
                                        $attributes[$key]['options'] = $color_result;
                                    }
                                }

                            }
                        }
                    }
                }
            endwhile;
            wp_reset_postdata();
        else :
            $error['message'] = esc_html__("No product found","pgs-woo-api");
            return $error;
        endif;

        $pgs_price_filters_status = (isset($pgs_woo_api_home_option['pgs_api_filters']['pgs_price']))?$pgs_woo_api_home_option['pgs_api_filters']['pgs_price']:'enable';
        $pgs_average_rating_status = (isset($pgs_woo_api_home_option['pgs_api_filters']['pgs_average_rating']))?$pgs_woo_api_home_option['pgs_api_filters']['pgs_average_rating']:'enable';

        $price_slider = array();
        if($pgs_price_filters_status == "enable"){
            $filtered_price = $this->pgs_get_filtered_price($args);
            $symbol = get_woocommerce_currency_symbol();
            $price_slider = array(
                'min_price' => $filtered_price->min_price,
                'max_price' => $filtered_price->max_price,
                'currency_symbol' => html_entity_decode($symbol)
            );
        }
        $rating_filters = array();
        if($pgs_average_rating_status == "enable"){

            $rating_filter = isset( $request['rating_filter'] ) ? array_filter( array_map( 'absint', explode( ',', wp_unslash( $request['rating_filter'] ) ) ) ) : array(); // WPCS: input var ok, CSRF ok, sanitization ok.
            for ( $rating = 5; $rating >= 1; $rating-- ) {
    			$count = $this->pgs_get_rating( $rating,$args,$rating_filter );
    			if ( empty( $count ) ) {
    				continue;
    			}
    			$found = true;


    			if ( in_array( $rating, $rating_filter, true ) ) {
    				$link_ratings = implode( ',', array_diff( $rating_filter, array( $rating ) ) );
    			} else {
    				$link_ratings = implode( ',', array_merge( $rating_filter, array( $rating ) ) );
    			}
                $class       = in_array( $rating, $rating_filter, true ) ? 'wc-layered-nav-rating chosen' : 'wc-layered-nav-rating';

    			$count_html  = esc_html( apply_filters( 'woocommerce_rating_filter_count', "{$count}", $count, $rating ) );
                $rating_filters[] = $rating;
            }
        }
        $filters = $attributes;
        if($pgs_average_rating_status == "enable"){
            $ratingfilters[] = array(
                'name' => 'Rating',
                'options' => $rating_filters
            );
            $filters = array_merge($attributes,$ratingfilters);
        }

        $data = array(
            'filters' => $filters,
            'price_filter_status' => $pgs_price_filters_status,
            'price_filter' => $price_slider,
            'rating_filters_status' => $pgs_average_rating_status
        );
        return $data;
    }

    public function pgs_woo_api_set_unique_associate_array($color_array) {
        $set_serialized_array = array_map("serialize", $color_array);
        foreach ($set_serialized_array as $key => $val) {
            $result[$val] = true;
        }
        return array_map("unserialize", (array_keys($result)));
    }

    /**
	 * Get the attributes for a product or product variation.
	 * @return array
	 */
	public function pgs_get_attributes( $product ) {
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
				if($attribute['is_taxonomy']){
                    $attributes[] = array(
    					'id'        => $attribute['is_taxonomy'] ? wc_attribute_taxonomy_id_by_name( $attribute['name'] ) : 0,
    					'name'      => $this->get_attribute_taxonomy_name( $attribute['name'], $product ),
    					'position'  => (int) $attribute['position'],
    					'visible'   => (bool) $attribute['is_visible'],
    					'variation' => (bool) $attribute['is_variation'],
    					'options'   => $this->pgs_get_attribute_options( $product->get_id(), $attribute ),
    				);
                }
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
	protected function pgs_get_attribute_options( $product_id, $attribute ) {

        if ( isset( $attribute['is_taxonomy'] ) && $attribute['is_taxonomy'] ) {

            if('pa_color' == $attribute['name']){
                    foreach($attribute['options'] as $options){

                    $name = get_term_by( 'id', $options, $attribute['name'] );
                    $value = get_term_meta( $options, 'color_code', true );
                    $data[] = array(
                        'color_code' => $value,
                        'color_name' => $name->name,
                    );
                }
                return $data;
            } else {
                return wc_get_product_terms( $product_id, $attribute['name'], array( 'fields' => 'names' ) );
            }
		} elseif ( isset( $attribute['value'] ) ) {
            return array_map( 'trim', explode( '|', $attribute['value'] ) );
		}
		return array();
	}


    /**
	 * Get product attribute taxonomy name.
	 *
	 * @param  string     $slug    Taxonomy name.
	 * @param  WC_Product $product Product data.
	 * @return string
	 */
	protected function get_attribute_taxonomy_name( $slug, $product ) {
		$attributes = $product->get_attributes( $slug );

		if ( ! isset( $attributes[ $slug ] ) ) {
			return str_replace( 'pa_', '', $slug );
		}

        if( 'pa_color' == $slug ){
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



    protected function pgs_get_filtered_price($args) {
        global $wpdb;

        //$args       = $wp_the_query->query_vars;
        $tax_query  = isset( $args['tax_query'] ) ? $args['tax_query'] : array();
        $meta_query = isset( $args['meta_query'] ) ? $args['meta_query'] : array();

        if ( ! empty( $args['taxonomy'] ) && ! empty( $args['term'] ) ) {
          $tax_query[] = array(
            'taxonomy' => $args['taxonomy'],
            'terms'    => array( $args['term'] ),
            'field'    => 'slug',
          );
        }

        foreach ( $meta_query + $tax_query as $key => $query ) {
          if ( ! empty( $query['price_filter'] ) || ! empty( $query['rating_filter'] ) ) {
            unset( $meta_query[ $key ] );
          }
        }

        $meta_query = new WP_Meta_Query( $meta_query );
        $tax_query  = new WP_Tax_Query( $tax_query );

        $meta_query_sql = $meta_query->get_sql( 'post', $wpdb->posts, 'ID' );
        $tax_query_sql  = $tax_query->get_sql( $wpdb->posts, 'ID' );

        $sql  = "SELECT min( FLOOR( price_meta.meta_value ) ) as min_price, max( CEILING( price_meta.meta_value ) ) as max_price FROM {$wpdb->posts} ";
        $sql .= " LEFT JOIN {$wpdb->postmeta} as price_meta ON {$wpdb->posts}.ID = price_meta.post_id " . $tax_query_sql['join'] . $meta_query_sql['join'];
        $sql .= " 	WHERE {$wpdb->posts}.post_type IN ('" . implode( "','", array_map( 'esc_sql', apply_filters( 'woocommerce_price_filter_post_type', array( 'product' ) ) ) ) . "')
    					AND {$wpdb->posts}.post_status = 'publish'
    					AND price_meta.meta_key IN ('" . implode( "','", array_map( 'esc_sql', apply_filters( 'woocommerce_price_filter_meta_keys', array( '_price' ) ) ) ) . "')
    					AND price_meta.meta_value > '' ";
        $sql .= $tax_query_sql['where'] . $meta_query_sql['where'];

        return $wpdb->get_row( $sql );
      }


      public function pgs_get_rating( $rating,$args,$rating_filter ) {
		global $wpdb;

		$tax_query  = isset( $args['tax_query'] ) ? $args['tax_query'] : array();
        $meta_query = isset( $args['meta_query'] ) ? $args['meta_query'] : array();

		// Unset current rating filter.
		foreach ( $tax_query as $key => $query ) {
			if ( ! empty( $query['rating_filter'] ) ) {
				unset( $tax_query[ $key ] );
				break;
			}
		}

		// Set new rating filter.
		$product_visibility_terms = wc_get_product_visibility_term_ids();
		$tax_query[]              = array(
			'taxonomy'      => 'product_visibility',
			'field'         => 'term_taxonomy_id',
			'terms'         => $product_visibility_terms[ 'rated-' . $rating ],
			'operator'      => 'IN',
			'rating_filter' => true,
		);

		$meta_query     = new WP_Meta_Query( $meta_query );
		$tax_query      = new WP_Tax_Query( $tax_query );
		$meta_query_sql = $meta_query->get_sql( 'post', $wpdb->posts, 'ID' );
		$tax_query_sql  = $tax_query->get_sql( $wpdb->posts, 'ID' );

		$sql  = "SELECT COUNT( DISTINCT {$wpdb->posts}.ID ) FROM {$wpdb->posts} ";
		$sql .= $tax_query_sql['join'] . $meta_query_sql['join'];
		$sql .= " WHERE {$wpdb->posts}.post_type = 'product' AND {$wpdb->posts}.post_status = 'publish' ";
		$sql .= $tax_query_sql['where'] . $meta_query_sql['where'];

		$search = WC_Query::get_main_search_query_sql();
		if ( $search ) {
			$sql .= ' AND ' . $search;
		}
		return absint( $wpdb->get_var( $sql ) ); // WPCS: unprepared SQL ok.
	}
 }
 new PGS_WOO_API_AttributesController;
