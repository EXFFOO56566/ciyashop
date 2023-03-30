<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_TestController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'test_api';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_test_api'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'test_notification', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_test_notification'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/test_api
    */
    public function pgs_woo_api_test_api( WP_REST_Request $request){
        $output =  "PGS Woo Api working fine";
        return $output;
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/test_notification
    * @param token : ####
    * @param message : ###
    * @param type : ###
    */
    public function pgs_woo_api_test_notification( WP_REST_Request $request){

        $data = $request->get_params();
        $required = array( 'token','message','type' );
        $validation = $this->pgs_woo_api_param_validation( $required, $data );
        if($validation){
           return $validation;
        }

        $token = (isset($request['token']))?$request['token']:'';
        $custom_msg = (isset($request['message']))?$request['message']:'Test notification alert!';
        $type = (isset($request['type']))?$request['type']:1;
        $msg = "PGS Woo API";
        $badge = 0;
        $device_data = array();
        $device_data[] = array(
            'token' => $token,
            'type' => $type
        );
        $not_code = 0;
        $this->send_push( $msg, $badge, $custom_msg,$not_code,$device_data);
        return true;
    }


}
new PGS_WOO_API_TestController;