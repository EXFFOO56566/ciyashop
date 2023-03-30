<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_OrdersController extends  PGS_WOO_API_Controller{
	/**
	 * Endpoint namespace.
	 *
	 * @var string
	 */
	protected $namespace = 'pgs-woo-api/v1';

	/**
	 * Route base.
	 *
	 * @var string
	 */
	protected $rest_base = 'orders';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

		// All Oders
        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_get_orders'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
		) );

        register_rest_route( $this->namespace, 'cancel_order', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_cancel_order'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/orders
    * @param customer: ####
    * @param page: ####
    */
    public function pgs_woo_api_get_orders(){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'customer','page' );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation) {
           return $validation;
        }


        $error = array( "status" => "error" );
        $user_id = $request['customer'];
        $post_num_page = 1;
        if(isset($request['page']) && !empty($request['page'])){
            $post_num_page = $request['page'];
        }


        if(isset($user_id) && !empty($user_id)){
			$post_per_page = 10;

            $args = array(
				'posts_per_page'   => $post_per_page,
				'paged'            => $post_num_page,
				'meta_key'    => '_customer_user',
				'meta_value'  => $user_id,
				'post_type'   => wc_get_order_types(),
				'post_status' => array_keys( wc_get_order_statuses() )
            );
			$customer_orders = get_posts( $args );
			$format_decimal    = array('total');
			$format_date       = array( 'date_created', 'date_modified', 'date_completed', 'date_paid' );

			$order_data = array();
			if(isset($customer_orders) && !empty($customer_orders)){
                foreach($customer_orders as $customer_order){
    				$id = $customer_order->ID;
    				$order = new WC_Order($id);
                    $data              = $order->get_data();
        			$format_decimal    = array( 'discount_total', 'discount_tax', 'shipping_total', 'shipping_tax', 'shipping_total', 'shipping_tax', 'cart_tax', 'total', 'total_tax' );
        			$format_date       = array( 'date_created', 'date_modified', 'date_completed', 'date_paid' );
        			$format_line_items = array( 'line_items', 'tax_lines', 'shipping_lines', 'fee_lines', 'coupon_lines' );

        			// Format decimal values.
        			foreach ( $format_decimal as $key ) {
        				$data[ $key ] = wc_format_decimal( $data[ $key ], 2);
        			}

        			// Format date values.
        			foreach ( $format_date as $key ) {
        				$datetime              = $data[ $key ];
        				$data[ $key ]          = wc_rest_prepare_date_response( $datetime, false );
        				$data[ $key . '_gmt' ] = wc_rest_prepare_date_response( $datetime );
        			}

        			// Format the order status.
        			$data['status'] = 'wc-' === substr( $data['status'], 0, 3 ) ? substr( $data['status'], 3 ) : $data['status'];

        			// Format line items.
        			foreach ( $format_line_items as $key ) {
        				$data[ $key ] = array_values( array_map( array( $this, 'get_order_item_data' ), $data[ $key ] ) );
        			}

        			// Refunds.
        			$data['refunds'] = array();
        			foreach ( $order->get_refunds() as $refund ) {
        				$data['refunds'][] = array(
        					'id'     => $refund->get_id(),
        					'refund' => $refund->get_reason() ? $refund->get_reason() : '',
        					'total'  => '-' . wc_format_decimal( $refund->get_amount(), 2 ),
        				);
        			}
                    $i=0;
                    foreach($data['line_items'] as $lineitems){
                        $src = get_the_post_thumbnail_url($lineitems['product_id']);
                        $lineitems['product_image'] = $src;
                        $data['line_items'][$i]['product_image'] = $src;
                        $i++;
                    }
                    $order_tracking_data = array();
                    $is_order_tracking_active = pgs_woo_api_is_order_tracking_active();
                    if( $is_order_tracking_active ){
                        //Get order tracking details
                        $order_tracking_data = $this->pgs_woo_api_get_order_tracking_data($order->get_id());
                        if(!empty($order_tracking_data)){
                            $order_tracking_data = array($order_tracking_data);
                        }
					}

                    $order_data[] = array(
            			'id'                   => $order->get_id(),
            			'parent_id'            => $data['parent_id'],
            			'number'               => $data['number'],
            			'order_key'            => $data['order_key'],
            			'created_via'          => $data['created_via'],
            			'version'              => $data['version'],
            			'status'               => $data['status'],
            			'currency'             => $data['currency'],
            			'date_created'         => $data['date_created'],
            			'date_created_gmt'     => $data['date_created_gmt'],
            			'date_modified'        => $data['date_modified'],
            			'date_modified_gmt'    => $data['date_modified_gmt'],
            			'discount_total'       => $data['discount_total'],
            			'discount_tax'         => $data['discount_tax'],
            			'shipping_total'       => $data['shipping_total'],
            			'shipping_tax'         => $data['shipping_tax'],
            			'cart_tax'             => $data['cart_tax'],
            			'total'                => $data['total'],
            			'total_tax'            => $data['total_tax'],
            			'prices_include_tax'   => $data['prices_include_tax'],
            			'customer_id'          => $data['customer_id'],
            			'customer_ip_address'  => $data['customer_ip_address'],
            			'customer_user_agent'  => $data['customer_user_agent'],
            			'customer_note'        => $data['customer_note'],
            			'billing'              => $data['billing'],
            			'shipping'             => $data['shipping'],
            			'payment_method'       => $data['payment_method'],
            			'payment_method_title' => $data['payment_method_title'],
            			'transaction_id'       => $data['transaction_id'],
            			'date_paid'            => $data['date_paid'],
            			'date_paid_gmt'        => $data['date_paid_gmt'],
            			'date_completed'       => $data['date_completed'],
            			'date_completed_gmt'   => $data['date_completed_gmt'],
            			'cart_hash'            => $data['cart_hash'],
            			'meta_data'            => $data['meta_data'],
            			'line_items'           => $data['line_items'],
            			'tax_lines'            => $data['tax_lines'],
            			'shipping_lines'       => $data['shipping_lines'],
            			'fee_lines'            => $data['fee_lines'],
            			'coupon_lines'         => $data['coupon_lines'],
            			'refunds'              => $data['refunds'],
						'order_tracking_data'  => $order_tracking_data
					);
                }
                wp_reset_postdata();
                return $order_data;
            } else {
                $error['message'] = esc_html__("No orders found","pgs-woo-api");
                return $error;
            }

		}
		else{
            $error['message'] = esc_html__("User not logged in","pgs-woo-api");
            return $error;
		}
	}

    /**
    * Get order tracking details
    */
    private function pgs_woo_api_get_order_tracking_data($order_id){
        $order_tracking_data = array();
        $options = get_option('aftership_option_name');


        $button = false;
        if(isset($options['use_track_button'])){
            $button = true;
        }

        if( is_array($options) ){
            if (array_key_exists('track_message_1', $options) && array_key_exists('track_message_2', $options)) {
                $track_message_1 = $options['track_message_1'] . '%s.';
                $track_message_2 = $options['track_message_2'] . '%s.';
            } else {
                $track_message_1 = esc_html__( 'Your order was shipped via %s.','pgs-woo-api' );
                $track_message_2 = esc_html__( 'Tracking number is %s.','pgs-woo-api' );
            }
            $custom_domain = '';
            if(isset($options['custom_domain'])&& !empty($options['custom_domain'])){
                $custom_domain = $this->addhttp_in_custom_domain($options['custom_domain']);
            }
        } else {
            $custom_domain = '';
            $track_message_1 = esc_html__( 'Your order was shipped via %s.','pgs-woo-api' );
            $track_message_2 = esc_html__( 'Tracking number is %s.','pgs-woo-api' );
            if(isset($options['custom_domain'])&& !empty($options['custom_domain'])){
                $custom_domain = $this->addhttp_in_custom_domain($options['custom_domain']);
            }
        }


        $tracking_provider_name = get_post_meta($order_id, '_aftership_tracking_provider_name', true);
        $tracking_number        = get_post_meta($order_id, '_aftership_tracking_number', true);

        if(isset($tracking_provider_name) && !empty($tracking_provider_name)){
			global $AfterShip;
			if ( $AfterShip && isset( $AfterShip->couriers ) && ! empty( $AfterShip->couriers ) ) {
				$courier_index   = array_search( $tracking_provider_name, array_column( $AfterShip->couriers, 'slug' ), true );
				$courier         = $AfterShip->couriers[ $courier_index ];
				$track_message_1 = sprintf( $track_message_1, $courier['name'] );
			} else {
				$track_message_1 = sprintf( $track_message_1, ucwords( $tracking_provider_name ) );
			}
        }
        $order_tracking_link = '';
        if(isset($tracking_number) && !empty($tracking_number)){
            $track_message_2 = sprintf( $track_message_2, $tracking_number );
            if($custom_domain != ''){
                $order_tracking_link = trailingslashit( $custom_domain ).$tracking_number;
            } else {
                $order_tracking_link = "https://track.aftership.com/$tracking_number";
            }

            $order_tracking_data = array(
                "use_track_button" => $button,
                "track_message_1" => $track_message_1,
                "track_message_2" => $tracking_number,
                "order_tracking_link" => $order_tracking_link
            );
        }
        return $order_tracking_data;
    }

    protected function addhttp_in_custom_domain($url) {
        if (!preg_match("~^(?:f|ht)tps?://~i", $url)) {
            $url = "https://" . $url;
        }
        return $url;
    }


    /**
	 * Expands an order item to get its data.
	 * @param WC_Order_item $item
	 * @return array
	 */
	protected function get_order_item_data( $item ) {
		$data           = $item->get_data();
		$format_decimal = array( 'subtotal', 'subtotal_tax', 'total', 'total_tax', 'tax_total', 'shipping_tax_total' );

		// Format decimal values.
		foreach ( $format_decimal as $key ) {
			if ( isset( $data[ $key ] ) ) {
				$data[ $key ] = wc_format_decimal( $data[ $key ], 2 );
			}
		}

		// Add SKU and PRICE to products.
		if ( is_callable( array( $item, 'get_product' ) ) ) {
			$data['sku']   = $item->get_product() ? $item->get_product()->get_sku(): null;
			$data['price'] = $item->get_total() / max( 1, $item->get_quantity() );
		}

		// Format taxes.
		if ( ! empty( $data['taxes']['total'] ) ) {
			$taxes = array();

			foreach ( $data['taxes']['total'] as $tax_rate_id => $tax ) {
				$taxes[] = array(
					'id'       => $tax_rate_id,
					'total'    => $tax,
					'subtotal' => isset( $data['taxes']['subtotal'][ $tax_rate_id ] ) ? $data['taxes']['subtotal'][ $tax_rate_id ] : '',
				);
			}
			$data['taxes'] = $taxes;
		} elseif ( isset( $data['taxes'] ) ) {
			$data['taxes'] = array();
		}

		// Remove names for coupons, taxes and shipping.
		if ( isset( $data['code'] ) || isset( $data['rate_code'] ) || isset( $data['method_title'] ) ) {
			unset( $data['name'] );
		}

		// Remove props we don't want to expose.
		unset( $data['order_id'] );
		unset( $data['type'] );

		return $data;
	}


    /**
	* Change Order status Processing to Cancelled by
	* Param Order id
	*/
	public function pgs_woo_api_cancel_order(){

		$input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'order' );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation) {
           return $validation;
        }


        $orderid = $request['order'];
		$checkorder = get_post($orderid);
        if($checkorder->post_status == "wc-on-hold")
		{
			$order = new WC_Order($orderid);
            $order->update_status('cancelled');
			$result = array(
				'result' => 'success',
				'message' => esc_html__( 'Order status changed to Cancelled','pgs-woo-api' )
			);
		}
		elseif($checkorder->post_status == "wc-pending"){
			$order = new WC_Order($orderid);
			$order->update_status('cancelled');
			$result = array(
				'result' => 'success',
				'message' => esc_html__( 'Order status changed to Cancelled','pgs-woo-api' ),
			);
		}
		else{
			$result = array(
				'result' => esc_html__( 'fail','pgs-woo-api' ),
				'message' => esc_html__( 'This Order status not is On-Hold or Pending','pgs-woo-api' )
			);
		}
		return $result;
	}
 }
 new PGS_WOO_API_OrdersController;
