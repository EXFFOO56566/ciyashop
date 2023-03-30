<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}
class PGS_WOO_API_LogoutController extends  PGS_WOO_API_Controller{
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
	protected $rest_base = 'logout';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
            'methods'             => WP_REST_Server::CREATABLE,//'POST',
			'callback'            => array( $this, 'pgs_woo_api_logout' ),
			'permission_callback' => array( $this, 'pgs_woo_api_logout_permission_callback' ),
    	) );
    }

	/**
	 * Check callback permission for the current request.
	 * @return boolean.
	 */
	public function pgs_woo_api_logout_permission_callback() {
        return true;
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/logout
    * Call this function only for webview user logout
    */
    public function pgs_woo_api_logout(){
        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $output = array();
        //Clear web user sesssion and cookies
        wp_destroy_current_session();
        wp_clear_auth_cookie();
        $output =  array(
            "status" => "success",
            "message" => esc_html__( 'User logged out successfully','pgs-woo-api')
        );
        return $output;
    }
 }
 new PGS_WOO_API_LogoutController();
