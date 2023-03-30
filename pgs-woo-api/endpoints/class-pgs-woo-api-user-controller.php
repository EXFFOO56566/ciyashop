<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_UserController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'update_user_image';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {
		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {


        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_update_user_image'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'customer', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_get_customer'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'create_customer', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_create_customer'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

		/*register_rest_route( $this->namespace, 'verify_customer', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_verify_customer'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );*/

        register_rest_route( $this->namespace, 'social_signup', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_social_signup'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'update_customer', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_update_customer'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/products
    * @param user_id: ####
    * @param user_image: ####
    */
    public function pgs_woo_api_update_user_image(){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $error = array( "status" => "error" );
        $required = array( 'user_id' );
        $user_id = $request['user_id'];
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }

        if ( isset($request['user_image']['data']) && !empty( $request['user_image']['data'] ) ) {

            $data = $this->get_upload_image_data($request['user_image'],$user_id);
            if($data) {
                $msg = esc_html__("Profile image updated successfully","pgs-woo-api");
            } else {
                $msg = esc_html__("Invalid image type","pgs-woo-api");
                $error['message'] = $msg;
                return $error;
            }
        } else {
            $msg = esc_html__("Please upload image","pgs-woo-api");
            $error['message'] = $msg;
            return $error;
        }


        $customer    = new WC_Customer( $user_id );
        $data        = $customer->get_data();
		$format_date = array( 'date_created', 'date_modified' );

		// Format date values.
		foreach ( $format_date as $key ) {
			$datetime              = $data[ $key ];
			$data[ $key ]          = wc_rest_prepare_date_response( $datetime, false );
			$data[ $key . '_gmt' ] = wc_rest_prepare_date_response( $datetime );
		}
        $url = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
        foreach($data['meta_data'] as $meta_data){
            if($meta_data->key == 'pgs_user_image'){

                if(isset($meta_data->value) && !empty($meta_data->value) ){
                    $src = $meta_data->value;
                    if(!empty($src)){
                        $url = esc_url($meta_data->value);
                    }else{
                        $url = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
                    }
                }
            }
        }

		return array(
			"status"             => "success",
            "message"            => $msg,
            'id'                 => $customer->get_id(),
			'date_created'       => $data['date_created'],
			'date_created_gmt'   => $data['date_created_gmt'],
			'date_modified'      => $data['date_modified'],
			'date_modified_gmt'  => $data['date_modified_gmt'],
			'email'              => $data['email'],
			'first_name'         => $data['first_name'],
			'last_name'          => $data['last_name'],
			'role'               => $data['role'],
			'username'           => $data['username'],
			'billing'            => $data['billing'],
			'shipping'           => $data['shipping'],
			'is_paying_customer' => $data['is_paying_customer'],
			'orders_count'       => $customer->get_order_count(),
			'total_spent'        => $customer->get_total_spent(),
			'avatar_url'         => $customer->get_avatar_url(),
			'meta_data'          => $data['meta_data'],
            'pgs_profile_image'  => $url
		);
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/create_customer
    * @param email : ####
    * @param username : ####
    * @param password : ####
    */
	public function pgs_woo_api_create_customer() {

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'email','username','password','mobile' );
        $error = array( "status" => "error" );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if ( $validation ) {
           return $validation;
        }

		if ( empty( $request['password'] ) ) {
			$random_password = wp_generate_password( 8, false );
			$request['password'] = $random_password;
		}

		$user_id = username_exists( trim(sanitize_text_field($request['username'])) );
		$email = trim(sanitize_text_field($request['email']));
        $password = trim(sanitize_text_field($request['password']));
		$mobile = trim(sanitize_text_field($request['mobile']));
		$username = sanitize_user( $request['username']);

		if ( username_exists( $username ) ) {
            $error['message'] = esc_html__('Username already exists', 'pgs-woo-api' );
            return $error;
        }

		if ( $user_id !== false ) {
            $error['message'] = esc_html__('Username already exists', 'pgs-woo-api' );
            return $error;
		}

		if ( isset( $email ) && email_exists( $email ) != false ) {
			$error['message'] = esc_html__('Email already exists', 'pgs-woo-api' );
            return $error;
		}

		if ( ! isset( $password ) ) {
			$error['message'] = esc_html__('Password required', 'pgs-woo-api' );
            return $error;
		}

		if ( ! isset( $mobile ) ) {
			$error['message'] = esc_html__('Phone number required', 'pgs-woo-api' );
            return $error;
		}

		if ( isset( $mobile ) && ! $this->validate_phone_number( $mobile ) ) {
			$error['message'] = esc_html__('Please enter correct phone number', 'pgs-woo-api' );
			return $error;
		}

		$user_id = wp_create_user( $username, $password, @$email );

		if ( ! is_wp_error( $user_id ) ) {
			$new_user = array(
				'ID' 			=> $user_id,
				'user_nicename' => @$request['user_nicename'],
				'display_name' 	=> @$request['display_name'],
				'nickname' 		=> @$request['nickname'],
				'first_name' 	=> @$request['first_name'],
				'last_name' 	=> @$request['last_name'],
				'role'          => 'customer',
			);

			if ( empty( $request['display_name'] ) && ( ! empty( $request['first_name'] ) || ! empty( $request['last_name'] ) ) ) {
				$new_user['display_name'] = trim(@$request['first_name'].' '.@$request['last_name']);
			}

			if ( empty( $request['user_nicename'] ) && ( ! empty( $request['first_name'] ) || ! empty( $request['last_name'] ) ) ) {
				$new_user['user_nicename'] = trim(@$request['first_name'].' '.@$request['last_name']);
			}

			if ( empty( $request['nickname'] ) && ( ! empty( $request['first_name'] ) || ! empty( $request['last_name'] ) ) ) {
				$new_user['nickname'] = trim(@$request['first_name'].' '.@$request['last_name']);
			}

			$user_id = wp_update_user( $new_user );

			$gender = ( isset( $request['gender'] ) ) ? trim( sanitize_text_field( $request['gender'] ) ) : '';
			$dob = ( isset( $request['dob'] ) ) ? trim( sanitize_text_field( $request['dob'] ) ) : '';

			update_user_meta( $user_id, 'mobile', $mobile );
			update_user_meta( $user_id, 'is_verified', 'yes' );
			update_user_meta( $user_id, 'gender', $gender );
			update_user_meta( $user_id, 'dob', $dob );


			if ( isset( $request['device_token'] ) && isset( $request['device_type'] ) ) {
				pgs_woo_api_add_push_notification_data( $request['device_token'], $request['device_type'], $user_id );
			}

			$user = get_userdata( $user_id );
			$seconds = 100;//14 days
			//$expiration = time() + apply_filters('auth_cookie_expiration', $seconds, $user->ID, true);
			//$cookie = wp_generate_auth_cookie($user->ID, $expiration, 'logged_in');
			$output =  array(
				"status" => "success",
				//"cookie" => $cookie,
				//"cookie_name" => LOGGED_IN_COOKIE,
				"user" => array(
					"id"           => $user->ID,
					"username"     => $user->user_login,
					"nicename"     => $user->user_nicename,
					"email"        => $user->user_email,
					"url"          => $user->user_url,
					"registered"   => $user->user_registered,
					"displayname"  => $user->display_name,
					"firstname"    => $user->user_firstname,
					"lastname"     => $user->last_name,
					"nickname"     => $user->nickname,
					"description"  => $user->user_description,
					"capabilities" => $user->wp_capabilities,
					"is_verified"  => 'yes'
				),
			);
			return $output;
		} else {
            $error['message'] = esc_html__('Invalid username.', 'pgs-woo-api' );
            return $error;
        }
	}
	/**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/verify_customer
    * @param user_id : ####
    * @param otp : ####
    */
	/*public function pgs_woo_api_verify_customer() {
		$input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'user_id' );
		$error = array( "status" => "error" );

		$validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }
	    $user_id = $request['user_id'];
		$is_verified = get_usermeta( $user_id, 'is_verified');
		$output =  array(
            "status" => "fail",
        );
		if( $is_verified == 'no' ) {
			update_user_meta( $user_id, 'is_verified', 'yes' );
			$output =  array(
				"status" => "success",
			);
		}
        return $output;
	}*/
    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/social_signup
    * @param email : ####
    * @param first_name : ####
    * @param last_name : ####
    */
    public function pgs_woo_api_social_signup() {


        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'email','first_name','last_name' );
        $error = array( "status" => "error" );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }

        $first_name = sanitize_text_field($request['first_name']);
        $last_name = sanitize_text_field($request['last_name']);
        $email = sanitize_text_field($request['email']);
        $user_name = strtolower($first_name.'.'.$last_name);

        while(username_exists($user_name)){
            $i++;
            $user_name = strtolower($first_name.'.'.$last_name).'.'.$i;
        }

		$username = sanitize_user( $user_name );

        if ( ! isset( $request['password'] ) ) {
			$random_password = wp_generate_password( 8, false );
			$request['password'] = $random_password;
		}

		if( isset( $request['email'] ) && email_exists( $request['email'] ) != false ) {
			$error['message'] = esc_html__('Email already exists', 'pgs-woo-api' );
            return $error;
		}

		if ( ! isset( $request['password'] ) ) {
			$error['message'] = esc_html__('Password required', 'pgs-woo-api' );
            return $error;
		}

		$user_id = wp_create_user( $username, $request['password'], @$request['email'] );

		if ( ! is_wp_error( $user_id ) ) {
			$new_user = array(
				'ID' 			=> $user_id,
				'user_nicename' => @$request['user_nicename'],
				'display_name' 	=> @$request['display_name'],
				'nickname' 		=> @$request['nickname'],
				'first_name' 	=> @$first_name,
				'last_name' 	=> @$last_name,
				'role'          => 'customer',
			);

			if ( empty( $request['display_name'] ) && ( ! empty( $first_name ) || ! empty( $last_name ) ) ) {
				$new_user['display_name'] = trim(@$first_name.' '.@$last_name);
			}

			if ( empty( $request['user_nicename'] ) && ( ! empty( $first_name ) || ! empty( $last_name ) ) ) {
				$new_user['user_nicename'] = trim(@$first_name.' '.@$last_name);
			}

			if ( empty( $request['nickname'] ) && ( ! empty( $first_name ) || ! empty( $last_name ) ) ) {
				$new_user['nickname'] = trim(@$first_name.' '.@$last_name);
			}

			$user_id = wp_update_user( $new_user );

			$mobile = ( isset( $request['mobile'] ) ) ? trim(sanitize_text_field( $request['mobile'] )) : '';
			$gender = ( isset( $request['gender'] ) ) ? trim(sanitize_text_field($request['gender'])) : '';
			$dob = ( isset( $request['dob'] ) ) ? trim(sanitize_text_field($request['dob'])) : '';
			$social_id = ( isset( $request['social_id'] ) ) ? trim(sanitize_text_field( $request['social_id'] )) : '';

			update_user_meta( $user_id, 'mobile', $mobile );
			update_user_meta( $user_id, 'gender', $gender );
			update_user_meta( $user_id, 'dob', $dob );
			update_user_meta( $user_id,'social_id',$social_id);
			if (isset($request['user_image']) && !empty( $request['user_image'] ) ) {
				$data = $this->get_upload_image_data($request['user_image'],$user_id);
			}

			if(isset($request['device_token']) && isset($request['device_type'])) {
				pgs_woo_api_add_push_notification_data($request['device_token'],$request['device_type'],$user_id);
			}

			$user = get_userdata( $user_id );
			$seconds = 100;//14 days
			$expiration = time() + apply_filters('auth_cookie_expiration', $seconds, $user->ID, true);
			$cookie = wp_generate_auth_cookie($user->ID, $expiration, 'logged_in');
			$output =  array(
				"status" => "success",
				"cookie" => $cookie,
				"cookie_name" => LOGGED_IN_COOKIE,
				"user" => array(
					"id" => $user->ID,
					"username" => $user->user_login,
					"nicename" => $user->user_nicename,
					"email" => $user->user_email,
					"url" => $user->user_url,
					"registered" => $user->user_registered,
					"displayname" => $user->display_name,
					"firstname" => $user->user_firstname,
					"lastname" => $user->last_name,
					"nickname" => $user->nickname,
					"description" => $user->user_description,
					"capabilities" => $user->wp_capabilities,
				),
			);
		} else {
			$error['message'] = esc_html__('Invalid username.', 'pgs-woo-api' );
			return $error;
		}
        return $output;
	}

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/update_customer
    * @param user_id : ####
    * @param first_name : ####
    * @param last_name : ####
    */
    public function pgs_woo_api_update_customer(  ) {


        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'user_id' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }

	    $user_id = $request['user_id'];

		$new_user = array(
			'ID' 			=> $user_id,
			'user_nicename' => @$request['user_nicename'],
			'display_name' 	=> @$request['display_name'],
			'nickname' 		=> @$request['nickname'],
			'first_name' 	=> @$request['first_name'],
			'last_name' 	=> @$request['last_name'],
		);
		if(empty($request['display_name']) && (!empty($request['first_name']) || !empty($request['last_name']))){
			$new_user['display_name'] = trim(@$request['first_name'].' '.@$request['last_name']);
		}
		if(empty($request['user_nicename']) && (!empty($request['first_name']) || !empty($request['last_name']))){
			$new_user['user_nicename'] = trim(@$request['first_name'].' '.@$request['last_name']);
		}
		if(empty($request['nickname']) && (!empty($request['first_name']) || !empty($request['last_name']))){
			$new_user['nickname'] = trim(@$request['first_name'].' '.@$request['last_name']);
		}

        $mobile = (isset($request['mobile']))?trim($request['mobile']):'';
        $gender = (isset($request['gender']))?trim($request['gender']):'';
        $dob = (isset($request['dob']))?trim($request['dob']):'';
        $user_id = wp_update_user( $new_user );
        update_user_meta( $user_id, 'mobile', $mobile );
        update_user_meta( $user_id, 'gender', $gender );
        update_user_meta( $user_id, 'dob', $dob );

        if(isset($request['billing']) && !empty($request['billing'])){
			$first_name = '';
			if(isset($request['billing']['first_name']) && !empty($request['billing']['first_name'])){
                $first_name = trim(sanitize_text_field($request['billing']['first_name']));
			}
			update_user_meta( $user_id, 'billing_first_name', $first_name );

			$last_name = '';
			if(isset($request['billing']['last_name']) && !empty($request['billing']['last_name'])){
                $last_name = trim(sanitize_text_field($request['billing']['last_name']));
			}
			update_user_meta( $user_id, 'billing_last_name', $last_name );

			$company = '';
            if(isset($request['billing']['company']) && !empty($request['billing']['company'])){
                $company = trim(sanitize_text_field($request['billing']['company']));
			}
			update_user_meta( $user_id, 'billing_company', $company );

			$address_1 = '';
            if(isset($request['billing']['address_1']) && !empty($request['billing']['address_1'])){
                $address_1 = trim(sanitize_text_field($request['billing']['address_1']));
			}
			update_user_meta( $user_id, 'billing_address_1', $address_1 );

			$address_2 = '';
            if(isset($request['billing']['address_2']) && !empty($request['billing']['address_2'])){
                $address_2 = trim(sanitize_text_field($request['billing']['address_2']));
			}
			update_user_meta( $user_id, 'billing_address_2', $address_2 );

			$city = '';
            if(isset($request['billing']['city']) && !empty($request['billing']['city'])){
                $city = trim(sanitize_text_field($request['billing']['city']));
			}
			update_user_meta( $user_id, 'billing_city', $city );

			$state = '';
            if(isset($request['billing']['state']) && !empty($request['billing']['state'])){
                $state = trim(sanitize_text_field($request['billing']['state']));
			}
			update_user_meta( $user_id, 'billing_state', $state );

			$postcode = '';
            if(isset($request['billing']['postcode']) && !empty($request['billing']['postcode'])){
                $postcode = trim(sanitize_text_field($request['billing']['postcode']));
			}
			update_user_meta( $user_id, 'billing_postcode', $postcode );

			$email = '';
            if(isset($request['billing']['email']) && !empty($request['billing']['email'])){
                $email = trim(sanitize_text_field($request['billing']['email']));
			}
			update_user_meta( $user_id, 'billing_email', $email );

			$phone = '';
			if(isset($request['billing']['phone']) && !empty($request['billing']['phone'])){
                $phone = trim(sanitize_text_field($request['billing']['phone']));
                update_user_meta( $user_id, 'billing_phone', $phone );
			}
			update_user_meta( $user_id, 'billing_phone', $phone );
        }

        if(isset($request['shipping']) && !empty($request['shipping'])){
			$first_name = '';
			if(isset($request['shipping']['first_name']) && !empty($request['shipping']['first_name'])){
                $first_name = trim(sanitize_text_field($request['shipping']['first_name']));
			}
			update_user_meta( $user_id, 'shipping_first_name', $first_name );

			$last_name = '';
            if(isset($request['shipping']['last_name']) && !empty($request['shipping']['last_name'])){
                $last_name = trim(sanitize_text_field($request['shipping']['last_name']));
			}
			update_user_meta( $user_id, 'shipping_last_name', $last_name );

			$company = '';
            if(isset($request['shipping']['company']) && !empty($request['shipping']['company'])){
                $company = trim(sanitize_text_field($request['shipping']['company']));
			}
			update_user_meta( $user_id, 'shipping_company', $company );

			$address_1 = '';
            if(isset($request['shipping']['address_1']) && !empty($request['shipping']['address_1'])){
                $address_1 = trim(sanitize_text_field($request['shipping']['address_1']));
			}
			update_user_meta( $user_id, 'shipping_address_1', $address_1 );

			$address_2 = '';
            if(isset($request['shipping']['address_2']) && !empty($request['shipping']['address_2'])){
                $address_2 = trim(sanitize_text_field($request['shipping']['address_2']));
			}
			update_user_meta( $user_id, 'shipping_address_2', $address_2 );

			$city = '';
            if(isset($request['shipping']['city']) && !empty($request['shipping']['city'])){
                $city = trim(sanitize_text_field($request['shipping']['city']));
			}
			update_user_meta( $user_id, 'shipping_city', $city );

			$state = '';
            if(isset($request['shipping']['state']) && !empty($request['shipping']['state'])){
                $state = trim(sanitize_text_field($request['shipping']['state']));
			}
			update_user_meta( $user_id, 'shipping_state', $state );

			$postcode = '';
            if(isset($request['shipping']['postcode']) && !empty($request['shipping']['postcode'])){
                $postcode = trim(sanitize_text_field($request['shipping']['postcode']));
			}
			update_user_meta( $user_id, 'shipping_postcode', $postcode );
        }

		$customer    = new WC_Customer( $user_id );
        $data        = $customer->get_data();
		$format_date = array( 'date_created', 'date_modified' );

		// Format date values.
		foreach ( $format_date as $key ) {
			$datetime              = $data[ $key ];
			$data[ $key ]          = wc_rest_prepare_date_response( $datetime, false );
			$data[ $key . '_gmt' ] = wc_rest_prepare_date_response( $datetime );
		}
        $url = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
        foreach($data['meta_data'] as $meta_data){
            if($meta_data->key == 'pgs_user_image'){

                if(isset($meta_data->value) && !empty($meta_data->value) ){
                    $src = $meta_data->value;
                    if(!empty($src)){
                        $url = esc_url($meta_data->value);
                    }else{
                        $url = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
                    }
                }
            }
        }

		return array(
			"status" => "success",
            "message" => esc_html__("User updated successfully","pgs-woo-api"),
            "user_id" => $user_id,
            'id'                 => $customer->get_id(),
			'date_created'       => $data['date_created'],
			'date_created_gmt'   => $data['date_created_gmt'],
			'date_modified'      => $data['date_modified'],
			'date_modified_gmt'  => $data['date_modified_gmt'],
			'email'              => $data['email'],
			'first_name'         => $data['first_name'],
			'last_name'          => $data['last_name'],
			'role'               => $data['role'],
			'username'           => $data['username'],
			'billing'            => $data['billing'],
			'shipping'           => $data['shipping'],
			'is_paying_customer' => $data['is_paying_customer'],
			'orders_count'       => $customer->get_order_count(),
			'total_spent'        => $customer->get_total_spent(),
			'avatar_url'         => $customer->get_avatar_url(),
			'meta_data'          => $data['meta_data'],
            'pgs_profile_image'  => $url
		);

	}

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/customer
    * @param user_id : ####
    */
    public function pgs_woo_api_get_customer() {


        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'user_id' );


        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }

        $customer    = new WC_Customer( $request['user_id'] );
        $customer->set_id($request['user_id']);
        $data        = $customer->get_data();

		$format_date = array( 'date_created', 'date_modified' );

		// Format date values.
		foreach ( $format_date as $key ) {
			$datetime              = $data[ $key ];
			$data[ $key ]          = wc_rest_prepare_date_response( $datetime, false );
			$data[ $key . '_gmt' ] = wc_rest_prepare_date_response( $datetime );
		}
        $url = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
        foreach($data['meta_data'] as $meta_data){
            if($meta_data->key == 'pgs_user_image'){

                if(isset($meta_data->value) && !empty($meta_data->value) ){
                    $src = $meta_data->value;
                    if(!empty($src)){
                        $url = esc_url($meta_data->value);
                    }else{
                        $url = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
                    }
                }
            }
        }

		$pgsiosappurl = get_option('pgs_ios_app_url');
        $pgs_ios_app_url = (isset($pgsiosappurl))?$pgsiosappurl:'';

        $output = array(
			'id'                 => $customer->get_id(),
			'date_created'       => $data['date_created'],
			'date_created_gmt'   => $data['date_created_gmt'],
			'date_modified'      => $data['date_modified'],
			'date_modified_gmt'  => $data['date_modified_gmt'],
			'email'              => $data['email'],
			'first_name'         => $data['first_name'],
			'last_name'          => $data['last_name'],
			'role'               => $data['role'],
			'username'           => $data['username'],
			'billing'            => $data['billing'],
			'shipping'           => $data['shipping'],
			'is_paying_customer' => $data['is_paying_customer'],
			'orders_count'       => $customer->get_order_count(),
			'total_spent'        => $customer->get_total_spent(),
			'avatar_url'         => $customer->get_avatar_url(),
			'meta_data'          => $data['meta_data'],
            'pgs_profile_image'  => $url,
            'ios_app_url'        => $pgs_ios_app_url
        );

        return apply_filters( 'pgs_woo_get_user_details', $output, $customer->get_id() );
	}

	/*
	* check validate mobile number.
	*/
	public function validate_phone_number($phone)
    {
        // Allow +, - and . in phone number
        $filtered_phone_number = filter_var($phone, FILTER_SANITIZE_NUMBER_INT);
        // Remove "-" from number
        $phone_to_check = str_replace("-", "", $filtered_phone_number);
        // Check the lenght of number
        // This can be customized if you want phone number from a specific country
        if (strlen($phone_to_check) < 10 || strlen($phone_to_check) > 14) {
           return false;
        } else {
          return true;
        }
    }

 }
 new PGS_WOO_API_UserController;
