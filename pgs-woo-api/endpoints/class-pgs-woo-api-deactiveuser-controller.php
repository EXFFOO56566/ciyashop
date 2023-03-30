<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_DeactiveuserController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'deactivate_user';

	public function __construct() {
		$this->register_routes();
        add_action( 'wp_login', array( $this, 'pgs_woo_api_before_user_login' ), 10, 2 );
        add_filter( 'login_message', array( $this, 'pgs_woo_api_deactive_user_message' ) );
	}
	public function register_routes() {
		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {


        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_deactivate_user'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/deactivate_user
    * @param user_id: ####
    * @param disable_user: ####
    * @param email: ####
    */
    public function pgs_woo_api_deactivate_user(){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'user_id','disable_user','email' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }

        if ( !isset( $request['disable_user'] ) ) {
			$disabled = 0;
		} else {
			$disabled = $request['disable_user'];
		}
        $flag = (isset($request['social_user']) && $request['social_user'] == 'yes')? false : true;


        if($flag){
            if ((!isset($request['email']) && !isset($request['password']))) {
            	$error['message'] = esc_html__("Please enter your email and password",'pgs-woo-api');
                return $error;

            }

            if ( empty($request['email']) ) {
            	$error['message'] = esc_html__("Please enter your email",'pgs-woo-api');
                return $error;

            }

        	if (!isset($request['password']) && empty($request['password'])) {
               $error['message'] = esc_html__("Please enter your password",'pgs-woo-api');
               return $error;

            }
            $user_obj = get_user_by( 'email', $request['email'] );
            $user = wp_authenticate($user_obj->data->user_login, $request['password']);
            if (is_wp_error($user)) {
                $error['message'] = esc_html__("Please enter valid email or password",'pgs-woo-api');
                return $error;
            }
        }

        $user_obj = get_user_by( 'email', $request['email'] );
        if($user_obj->ID != $request['user_id']){
            $error['message'] = esc_html__("You are not allowed to do that",'pgs-woo-api');
            return $error;
        }


		$data = update_user_meta( $request['user_id'], 'pgs_woo_api_disable_user', $disabled );
        $output =  array(
                "status" => "success",
                "message" => esc_html__("Your account successfully deactivated",'pgs-woo-api')
        );
        return $output;
    }

    public function pgs_woo_api_before_user_login( $user_login, $user = null ){
        if ( !$user ) {
			$user = get_user_by('login', $user_login);
		}
		if ( !$user ) {
			// not logged in - definitely not disabled
			return;
		}

        if (!is_object($user)) {
            return;
        }
		// Get user meta
		$disabled = get_user_meta( $user->ID, 'pgs_woo_api_disable_user', true );
		$error = array( "status" => "error" );
		// Is the use logging in disabled?
		if ( $disabled == '1' ) {
			// Clear cookies, a.k.a log user out
			wp_clear_auth_cookie();

			// Build login URL and then redirect
			$login_url = site_url( 'wp-login.php', 'login' );
			$login_url = add_query_arg( 'disabled', '1', $login_url );
			wp_redirect( $login_url );
			exit;

		} else {
            return;
		}
    }

    /**
	 * Show a notice to users who try to login and are disabled
	 *
	 * @param string $message
	 * @return string
	 */
	public function pgs_woo_api_deactive_user_message( $message ) {

		// Show the error message if it seems to be a disabled user
		if ( isset( $_GET['disabled'] ) && $_GET['disabled'] == 1 )
			$message =  '<div id="login_error">' . esc_html__( 'Account disabled', 'pgs-woo-api' ) . '</div>';

		return $message;
	}
 }
 new PGS_WOO_API_DeactiveuserController;