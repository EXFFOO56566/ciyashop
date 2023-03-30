<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}
class PGS_WOO_API_LoginController extends  PGS_WOO_API_Controller{
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
	protected $rest_base = 'login';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
            'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_login_validate'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'social_login', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_social_login_validate'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/login
    * @param String email: email
    * @param String password: password
    * @param device_token : ####
    * @param device_type : ####
    */
    public function pgs_woo_api_login_validate(){
		$input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'email','password' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }

        $output = array();
        $error = array( "status" => "error" );

        if ((!isset($request['email']) && !isset($request['password']))) {
        	$error['message'] = esc_html__("Please enter your email and password",'pgs-woo-api');
            return $error;
        }

        if ((!isset($request['email']) && empty($request['email']))) {
        	$error['message'] = esc_html__("Please enter your email",'pgs-woo-api');
            return $error;

        }

    	if (!isset($request['password']) && empty($request['password'])) {
           $error['message'] = esc_html__("Please enter your password",'pgs-woo-api');
           return $error;

        }

        $seconds = 100;//14 days

        if ( $request['email'] ) {


            if ( is_email(  $request['email']) ) {
                if( !email_exists( $request['email']))  {
                    $error['message'] = esc_html__("Email does not exist.",'pgs-woo-api');
                    return $error;
                }
            } else {
                $error['message'] = esc_html__("Please enter valid email address.",'pgs-woo-api');
                return $error;

            }

            $user_obj = get_user_by( 'email', $request['email'] );

            $disabled = get_user_meta( $user_obj->ID, 'pgs_woo_api_disable_user', true );

    		// Is the use logging in disabled?
    		if ( $disabled == '1' ) {
                $error['message'] = esc_html__("You are currently deactivated. Please contact to admin for active account.",'pgs-woo-api');
                return $error;
    		}
            $user = wp_authenticate($user_obj->data->user_login, $request['password']);
            if (is_wp_error($user)) {
				if ( $user_obj instanceof WP_User && isset( $user_obj->ID ) && 'yes' === get_user_meta( (int) $user_obj->ID, 'user_marked_as_deleted', true ) ) {
					$error['message'] = esc_html__( 'You are not allowed to log in. Your account deletion request is under process.' ,'pgs-woo-api' );
				} else {
					$error['message'] = esc_html__( 'Please enter valid email or password.', 'pgs-woo-api' );
				}
                return $error;

            }
        }


        if(isset($request['device_token']) && isset($request['device_type'])) {
            pgs_woo_api_add_push_notification_data($request['device_token'],$request['device_type'],$user->ID);
    	}


        //Clear web user sesssion and cookies
        wp_destroy_current_session();
        wp_clear_auth_cookie();

        $expiration = time() + apply_filters('auth_cookie_expiration', $seconds, $user->ID, true);
        $cookie = wp_generate_auth_cookie($user->ID, $expiration, 'logged_in');

        $mobile = get_user_meta( $user->ID, 'mobile', true );
        $gender = get_user_meta( $user->ID, 'gender', true );
        $dob = get_user_meta( $user->ID, 'dob', true );
        $pgs_user_image = get_user_meta( $user->ID, 'pgs_user_image', true );


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
                "mobile" => $mobile,
                "gender" => $gender,
                "dob" => $dob,
                "pgs_user_image" => $pgs_user_image
            ),
        );

        return $output;
    }



    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/social_login
    * @param String email: email
    * @param String Fb or Google ID social_id : #####
    */
    function pgs_woo_api_social_login_validate(){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'email' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }

        $output = array();
        $error = array( "status" => "error" );

        if ((!isset($request['email']) && empty($request['email']))) {
            $error['message'] = esc_html__("Please enter your email",'pgs-woo-api');
            return $error;
        }

    	if (!isset($request['social_id']) && empty($request['social_id'])) {
           $error['message'] = esc_html__("Something went wrong. Please try later",'pgs-woo-api');
           return $error;
        }

		$first_name = '';
		$last_name = '';
		if ( isset($request['first_name']) && !empty($request['first_name']) ) {
			$first_name = $request['first_name'];
		}
		if ( isset($request['last_name']) && !empty($request['last_name']) ) {
			$last_name = $request['last_name'];
		}
        $seconds = 1209600;//14 days
        $social_id = '';
        if ( $request['email'] ) {


            if ( is_email(  $request['email']) ) {
                $email_exists = email_exists( $request['email'] );
                if( !$email_exists )  {
                    return $this->pgs_woo_api_login_with_social_signup($request);
                } else {
					update_user_meta( $email_exists, 'social_id', $request['social_id'] );
				}
            } else {
                $error['message'] = esc_html__( 'Please enter valid email address.', 'pgs-woo-api');
                return $error;

            }

            $user_obj = get_user_by( 'email', $request['email'] );
            $social_id = get_user_meta($user_obj->ID,'social_id',true);
            if(!$social_id){
                $error['error'] = esc_html__("Something went wrong. Please try later.","pgs-woo-api");
                return $error;
            }

            $disabled = get_user_meta( $user_obj->ID, 'pgs_woo_api_disable_user', true );

    		// Is the use logging in disabled?
    		if ( $disabled == '1' ) {
                $error['message'] = esc_html__("You are currently deactivated. Please contact to admin for active account",'pgs-woo-api');
                return $error;
    		}

            if ( !$user_obj->ID && $email_exists == false)  {
                $error['error'] = esc_html__("Something went wrong. Please try later.","pgs-woo-api");
                return $error;
            }

            /*if ( $social_id !=  $request['social_id'] )  {
                $error['message'] = esc_html__("User validation error. Please try later",'pgs-woo-api');
                return $error;
            }*/
        }
        if ( $user_obj->ID && $social_id != '') {
			update_user_meta( $user_obj->ID, 'first_name', $first_name );
			update_user_meta( $user_obj->ID, 'last_name', $last_name );
			if(isset($request['device_token']) && isset($request['device_type'])) {
				pgs_woo_api_add_push_notification_data($request['device_token'],$request['device_type'],$user_obj->ID);
			}

            //Clear web user sesssion and cookies
            wp_destroy_current_session();
            wp_clear_auth_cookie();

            $expiration = time() + apply_filters('auth_cookie_expiration', $seconds, $user_obj->ID, true);
            $cookie = wp_generate_auth_cookie($user_obj->ID, $expiration, 'logged_in');

            $output =  array(
                "status" => "success",
                "cookie" => $cookie,
                "cookie_name" => LOGGED_IN_COOKIE,
                "user" => array(
                    "id" => $user_obj->ID,
                    "username" => ($user_obj->user_login)?$user_obj->user_login:'',
                    "nicename" => ($user_obj->user_nicename)?$user_obj->user_nicename:'',
                    "email" => $user_obj->user_email,
                    "url" => $user_obj->user_url,
                    "registered" => $user_obj->user_registered,
                    "displayname" => ($user_obj->display_name)?$user_obj->display_name:'',
                    "firstname" => ($user_obj->first_name)?$user_obj->first_name:$first_name,
                    "lastname" => ($user_obj->last_name)?$user_obj->last_name:$last_name,
                    "nickname" => ($user_obj->nickname)?$user_obj->nickname:'',
                    "description" => $user_obj->user_description,
                    "capabilities" => $user_obj->wp_capabilities
                ),
            );
        } else {
            $error['error'] = esc_html__("Something went wrong. Please try later.","pgs-woo-api");
            return $error;
        }

        return $output;
    }

    function pgs_woo_api_login_with_social_signup($request){
        $required = array( 'email' );
        $error = array( "status" => "error" );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }
        $user_name = '';
        $first_name = sanitize_text_field($request['first_name']);
        $last_name = sanitize_text_field($request['last_name']);
        $email = sanitize_text_field($request['email']);
        $user_name = strtolower($first_name.'.'.$last_name);

		if ( empty($user_name) || $user_name == '.' ) {
            if(isset($email) && !empty($email)){
                $parts = explode("@", $email);
                $user_name = $parts[0];
            }
		}

        while(username_exists($user_name)){
            $i++;
            $user_name = strtolower($first_name.'.'.$last_name).'.'.$i;
		}

		$username = sanitize_user( $user_name );


        if ( ! isset($request['password']) ) {
			$random_password = wp_generate_password( 8, false );
			$request['password'] = $random_password;
		}

		if ( isset( $request['email'] ) && email_exists( $request['email'] ) != false ) {
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
			if(empty($request['display_name']) && (!empty($first_name) || !empty($last_name))){
				$new_user['display_name'] = trim(@$first_name.' '.@$last_name);
			}
			if(empty($request['user_nicename']) && (!empty($first_name) || !empty($last_name))){
				$new_user['user_nicename'] = trim(@$first_name.' '.@$last_name);
			}
			if(empty($request['nickname']) && (!empty($first_name) || !empty($last_name))){
				$new_user['nickname'] = trim(@$first_name.' '.@$last_name);
			}
			$user_id = wp_update_user( $new_user );

			$mobile = (isset($request['mobile']))?trim(sanitize_text_field($request['mobile'])):'';
			$gender = (isset($request['gender']))?trim(sanitize_text_field($request['gender'])):'';
			$dob = (isset($request['dob']))?trim(sanitize_text_field($request['dob'])):'';
			$social_id = (isset($request['social_id']))?trim(sanitize_text_field($request['social_id'])):'';

			update_user_meta( $user_id, 'first_name', $first_name );
			update_user_meta( $user_id, 'last_name', $last_name );

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
					"username" => ($user->user_login)?$user->user_login:'',
					"nicename" => ($user->user_nicename)?$user->user_nicename:'',
					"email" => $user->user_email,
					"url" => $user->user_url,
					"registered" => $user->user_registered,
					"displayname" => ($user->display_name)?$user->display_name:'',
					"firstname" => ($user->first_name)?$user->first_name:'',
					"lastname" => ($user->last_name)?$user->last_name:'',
					"nickname" => ($user->nickname)?$user->nickname:'',
					"description" => $user->user_description,
					"capabilities" => $user->wp_capabilities,
				),
			);
			return $output;
		} else {
            $error['message'] = esc_html__('Invalid username.', 'pgs-woo-api' );
            return $error;
        }
    }
}
new PGS_WOO_API_LoginController;
