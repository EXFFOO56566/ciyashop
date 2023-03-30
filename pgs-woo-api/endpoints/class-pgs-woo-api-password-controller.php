<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_PasswordController extends  PGS_WOO_API_Controller{
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
	protected $rest_base = 'reset_password';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_reset_password'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'forgot_password', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this,'pgs_woo_api_forgot_password'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'update_password', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this,'pgs_woo_api_update_password'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

    }


    /**
    * Reset user password
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/reset_password
    * @param user_id: user id
    * @param password: password
    */
    function pgs_woo_api_reset_password( WP_REST_Request $request ){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'user_id','password' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }



        $output = array();
        $error = array( "status" => "error" );

        $nonce = wp_create_nonce( 'wp_rest' );
        if ((isset($request['user_id']) && empty($request['user_id']))) {
        	$error['error'] = esc_html__("Something went wrong. Please try later.","pgs-woo-api");
            return $error;

        }

    	if (isset($request['password']) && empty($request['password'])) {
           $error['message'] = esc_html__("Please enter password.","pgs-woo-api");
           return $error;

        }

        $user_data = get_user_by( 'ID', trim( $request['user_id'] ) );

        if ( empty( $user_data ) ) {
            $error['message'] = esc_html__("Something went wrong. Please try later.","pgs-woo-api");
            return $error;

        }

        $user_login = $user_data->user_login;
        $user_email = $user_data->user_email;
        do_action('retrieve_password', $user_login);

        $allow = apply_filters('allow_password_reset', true, $user_data->ID);

        if ( ! $allow ){
            $error['message'] = esc_html__("password reset not allowed!","pgs-woo-api");
            return $error;

        } elseif ( is_wp_error($allow) ) {
            $error['message'] = esc_html__("An error occurred!","pgs-woo-api");
            return $error;

        }
        wp_set_password( $request['password'], $user_data->ID );
        return array(
                "status" => "success",
                "message" => esc_html__('Your password has been reset.',"pgs-woo-api")
            );
    }


/**
    *
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/forgot_password
    * @param email: user email
    */
    function pgs_woo_api_forgot_password( WP_REST_Request $request ){
        global $wpdb;
        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'email' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation) {
           return $validation;
        }


        $output = array();
        $error = array( "status" => "error" );

        $nonce = wp_create_nonce( 'wp_rest' );
        if (isset($request['email']) && empty($request['email'])) {
        	$error['message'] = esc_html__("Please enter valid email.","pgs-woo-api");
            return $error;

        }

        if(!is_email($request['email'])){
            $error['message'] = esc_html__("Please enter valid email.","pgs-woo-api");
            return $error;
        }

        $user_data = get_user_by( 'email', trim( $request['email'] ) );

        if ( empty( $user_data ) ) {
            $error['message'] = esc_html__("Please enter valid registered email.","pgs-woo-api");
            return $error;
        }

        $user_login = $user_data->user_login;
        $user_email = $user_data->user_email;
        do_action('retrieve_password', $user_login);

        $allow = apply_filters('allow_password_reset', true, $user_data->ID);

        if ( ! $allow ){
            $error['message'] = esc_html__( 'password reset not allowed!', 'pgs-woo-api' );
            return $error;

        } elseif ( is_wp_error($allow) ) {
            $error['message'] = esc_html__( 'An error occurred!', 'pgs-woo-api' );
            return $error;

        }

        $key = wp_generate_password( 8, false );
        update_user_meta( $user_data->ID,'pgs_woo_api_user_auth_token',$key);

        $message = esc_html__('Someone requested that the password be reset for the following account:','pgs-woo-api') . "\r\n\r\n";
        $message .= sprintf( __( 'Username: %s', 'pgs-woo-api' ), $user_login ) . "\r\n\r\n";
        $message .= esc_html__('If this was a mistake, just ignore this email and nothing will happen.','pgs-woo-api') . "\r\n\r\n";
        $message .= esc_html__("To reset your password, pass this key : $key",'pgs-woo-api') . "\r\n\r\n";


        $forgot_password_mail_options_data = pgs_woo_api_get_forgot_password_mail_options_data();
        $forgot_password_subject = $forgot_password_mail_options_data['forgot_password_subject'];
        $forgot_password_from_name = $forgot_password_mail_options_data['forgot_password_from_name'];
        $forgot_password_from_email = $forgot_password_mail_options_data['forgot_password_from_email'];

        $title = apply_filters('retrieve_password_title', $forgot_password_subject);
        $message = apply_filters('retrieve_password_message', $message, $key);

        $headers[] = 'Content-Type: text/html; charset=UTF-8';
        $headers[] = 'From: '.$forgot_password_from_name.' <'.$forgot_password_from_email.'>';

        if ( $message && !wp_mail($user_email, $title, $message, $headers) ){
            $error['error'] = esc_html__("The e-mail could not be sent.Please try after some time.",'pgs-woo-api');
            return $error;
        } else {
            return array(
                "status" => "success",
                "message" => esc_html__('Key for password reset has been emailed to you. Please check your email.','pgs-woo-api'),
                "key" => $key,
            );
        }

    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/update_password
    * @param email: email
    * @param password: password
    * @param key: key
    */
    function pgs_woo_api_update_password(){

        $output = array();
        $error = array( "status" => "error" );

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'email','password' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation) {
           return $validation;
        }



        if (isset($request['email']) && empty($request['email'])) {
        	$error['message'] = esc_html__("Something went wrong. Please try later.","pgs-woo-api");
            return $error;

        }

    	if (isset($request['password']) && empty($request['password'])) {
           $error['message'] = esc_html__("Please enter password.","pgs-woo-api");
           return $error;

        }


        $user_data = get_user_by( 'email', trim( $request['email'] ) );

        if ( empty( $user_data ) ) {
            $error['message'] = esc_html__("Something went wrong. Please try later.","pgs-woo-api");
            return $error;

        }

        $user_login = $user_data->user_login;
        $user_email = $user_data->user_email;
        $auth_key = get_user_meta($user_data->ID,'pgs_woo_api_user_auth_token',true);
        $allow = apply_filters('allow_password_reset', true, $user_data->ID);

        if ( ! $allow ){
            $error['message'] = esc_html__("password reset not allowed!","pgs-woo-api");
            return $error;

        } elseif ( is_wp_error($allow) ) {
            $error['message'] = esc_html__("An error occurred!","pgs-woo-api");
            return $error;

        }

        if ($request['key'] != $auth_key){
    		$error['message'] = esc_html__("Something went wrong. Please try later.", "pgs-woo-api");
            return $error;
        } else {
            wp_set_password( $request['password'], $user_data->ID );
        }

        return array(
            "status" => "success",
            "message" => esc_html__('Your password has been reset.','pgs-woo-api')
        );

    }
}

new PGS_WOO_API_PasswordController();
