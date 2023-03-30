<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_CouponsController extends  PGS_WOO_API_Controller{
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
	protected $rest_base = 'coupons';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_get_coupons'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'scratch_coupon', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_update_scratch_coupon'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/coupons
    * @param page: ####
    * @param device_token : ####
    * @param user_id : ####
    *
    */
    public function pgs_woo_api_get_coupons(){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $error = array( "status" => "error" );
        $post_num_page = 1;
        if(isset($request['page']) && !empty($request['page'])){
            $post_num_page = $request['page'];
        }

        $device_token = '';
        if(isset($request['device_token']) && !empty($request['device_token'])){
            $device_token = $request['device_token'];
        }

        $user_id = 0;
        if(isset($request['user_id']) && !empty($request['user_id'])){
            $user_id = $request['user_id'];
        }

		$post_per_page = 10;

        $args = array(
			'posts_per_page'   => $post_per_page,
			'paged'            => $post_num_page,
			'post_type'   => 'shop_coupon',
			'post_status' => 'publish'
        );
		$shop_coupons = get_posts( $args );
		$coupon_data = array();$cupons = array();
		if(isset($shop_coupons) && !empty($shop_coupons)){
            $data['status'] = 'success';
            $data['message'] = esc_html__("Result successfully fetched","pgs-woo-api");
            foreach($shop_coupons as $shop_coupon){
                $id = $shop_coupon->ID;
                $hide_coupon_on_app = get_post_meta( $id, 'hide_coupon_on_app', true );
                if($hide_coupon_on_app != 'yes'){
                    $cupons[] = $this->pgs_woo_api_get_coupon_data(array('id' => $id),$device_token,$user_id);
                }
            }
            $data['data'] = $cupons;
            $response = rest_ensure_response( $data );
        	wp_reset_postdata();
            return $response;
        } else {
            $error['status'] = 'success';
            $error['message'] = esc_html__("No Coupons found","pgs-woo-api");
            return $error;
        }
    }

    /**
     * Get coupon data id wise
     */
    function pgs_woo_api_get_coupon_data($parameters,$device_token='',$user_id=''){
        $coupon = new WC_Coupon( $parameters['id'] );
		$data = $coupon->get_data();
		$format_decimal = array( 'amount', 'minimum_amount', 'maximum_amount' );
		$format_date    = array( 'date_created', 'date_modified', 'date_expires' );
		$format_null    = array( 'usage_limit', 'usage_limit_per_user', 'limit_usage_to_x_items' );

		// Format decimal values.
		foreach ( $format_decimal as $key ) {
			$data[ $key ] = wc_format_decimal( $data[ $key ], 2 );
		}

		// Format date values.
		foreach ( $format_date as $key ) {
			$datetime 	  			= $data[ $key ];
			$data[ $key ] 			= wc_rest_prepare_date_response( $datetime, false );
			$data[ $key . '_gmt' ] 	= wc_rest_prepare_date_response( $datetime );
		}

		// Format null values.
		foreach ( $format_null as $key ) {
			$data[ $key ] = $data[ $key ] ? $data[ $key ] : null;
		}

        $is_coupon_scratched = $this->pgs_woo_api_get_scratch_coupon_meta($coupon->get_id(),$device_token,$user_id);

		return array(
			'id'                          => $coupon->get_id(),
			'code'                        => $data['code'],
			'amount'                      => $data['amount'],
			'date_created'                => $data['date_created'],
			'date_created_gmt'            => $data['date_created_gmt'],
			'date_modified'               => $data['date_modified'],
			'date_modified_gmt'           => $data['date_modified_gmt'],
			'discount_type'               => $data['discount_type'],
			'description'                 => $data['description'],
			'date_expires'                => $data['date_expires'],
			'date_expires_gmt'            => $data['date_expires_gmt'],
			'usage_count'                 => $data['usage_count'],
			'individual_use'              => $data['individual_use'],
			'product_ids'                 => $data['product_ids'],
			'excluded_product_ids'        => $data['excluded_product_ids'],
			'usage_limit'                 => $data['usage_limit'],
			'usage_limit_per_user'        => $data['usage_limit_per_user'],
			'limit_usage_to_x_items'      => $data['limit_usage_to_x_items'],
			'free_shipping'               => $data['free_shipping'],
			'product_categories'          => $data['product_categories'],
			'excluded_product_categories' => $data['excluded_product_categories'],
			'exclude_sale_items'          => $data['exclude_sale_items'],
			'minimum_amount'              => $data['minimum_amount'],
			'maximum_amount'              => $data['maximum_amount'],
			'email_restrictions'          => $data['email_restrictions'],
			'used_by'                     => $data['used_by'],
			'meta_data'                   => $data['meta_data'],
            'is_coupon_scratched'         => $is_coupon_scratched,
		);
    }


    /**
    * Get coupon meta
    * @param coupon_id: ####
    * @param is_coupon_scratched : yes/no
    * @param device_token : ####
    * @param user_id : ####
    */
    public function pgs_woo_api_get_scratch_coupon_meta($coupon_id,$device_token,$user_id=''){
        global $wpdb;
        $and='';$and='';
        $u_id=0;
        if(!empty($user_id)){
            $u_id = (int)$user_id;
        }
        $and = " AND user_id=".$u_id;
        $table_name = $wpdb->prefix . "pgs_woo_api_scratch_coupons";
        $qur = "SELECT is_coupon_scratched FROM $table_name WHERE coupon_id = $coupon_id AND device_token = '$device_token'$and";
        $results = $wpdb->get_row( $qur, OBJECT );

        $is_coupon_scratched = 'no';
        if(!empty($results)){
            $is_coupon_scratched = $results->is_coupon_scratched;
        }
        return $is_coupon_scratched;
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/scratch_coupon
    * @param coupon_id: ####
    * @param is_coupon_scratched : yes/no
    * @param device_token : ####
    * @param user_id : #### //optional
    */
    public function pgs_woo_api_update_scratch_coupon(){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'coupon_id','is_coupon_scratched','device_token');
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation) {
           return $validation;
        }

        $error = array( "status" => "error" );

        $device_token = $request['device_token'];
        $coupon_id = $request['coupon_id'];
        $device_token = $request['device_token'];
        $is_coupon_scratched = $request['is_coupon_scratched'];
        $user_id = 0;
        $post_num_page = 1;
        if(isset($request['user_id']) && !empty($request['user_id'])){
            $user_id = $request['user_id'];
        }
        global $wpdb;
        $table_name = $wpdb->prefix . "pgs_woo_api_scratch_coupons";
        $qur = "SELECT * FROM $table_name WHERE coupon_id = $coupon_id AND device_token = '$device_token'";
        $results = $wpdb->get_results( $qur, OBJECT );
        $data = array(
            'coupon_id' => $coupon_id,
            'user_id' => $user_id,
            'device_token' => $device_token,
            'is_coupon_scratched' => $is_coupon_scratched
        );
        $formate = array( '%d','%d','%s','%s' );

        if(!empty($results)){
            $wpdb->update($table_name, $data, array('device_token' => $device_token),$formate,array('%s'));
        } else {
            $wpdb->insert( $table_name,$data,$formate );
        }
        $result = array(
            "status" => "success",
            "message" => esc_html__('Coupon scratched successfully.',"pgs-woo-api")
        );
        return $result;
    }
 }
 new PGS_WOO_API_CouponsController;