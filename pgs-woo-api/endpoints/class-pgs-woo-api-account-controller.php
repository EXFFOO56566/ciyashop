<?php
/**
 * Account controller.
 *
 * @package PGS Woo Api/endpoints
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

/**
 * PGS_WOO_API_Account_Controller class.
 */
class PGS_WOO_API_Account_Controller extends PGS_WOO_API_Controller {
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
	protected $rest_base = 'delete_account';

	/**
	 * PGS Woo API version.
	 *
	 * @var string
	 */
    private $app_ver = '';

	/**
	 * WP_REST_Request object.
	 *
	 * @var WP_REST_Request
	 */
    private $wp_rest_request = null;

	/**
	 * Request params.
	 *
	 * @var array
	 */
    private $params = array();

	/**
	 * User ID.
	 *
	 * @var int
	 */
    private $user_id = '';

	/**
	 * Constructor.
	 */
	public function __construct() {
		// Delete cccount hooks.
		add_action( 'save_post', array( $this, 'save_post' ), 10, 3 );
		add_filter( 'user_request_action_confirmed', array( $this, 'user_request_confirmed' ) );
		add_action( 'wp_privacy_personal_data_erased', array( $this, 'after_personal_data_erased' ), 999999 );
		add_filter( 'manage_users_custom_column', array( $this, 'users_custom_column' ), 999999, 3 );
		add_filter( 'user_request_action_description', array( $this, 'request_action_description' ), 10, 2 );
		add_filter( 'wp_authenticate_user', array( $this, 'check_lock' ), 5, 2 );
		add_action( 'lostpassword_post', array( $this, 'check_lost_password' ), 10, 2 );

		// Register routes.
		add_action( 'rest_api_init', array( $this, 'register_routes' ) );
	}

	/**
	 * Register routes function.
	 */
	public function register_routes() {

		register_rest_route(
			$this->namespace,
			$this->rest_base,
			array(
				'methods'             => WP_REST_Server::CREATABLE, // 'POST',
				'callback'            => array( $this, 'delete_account_callback' ),
				'permission_callback' => array( $this, 'pgs_woo_api_permission_callback' ),
			)
		);

		register_rest_route(
			$this->namespace,
			'is_user_exists',
			array(
				'methods'             => WP_REST_Server::CREATABLE, // 'POST',
				'callback'            => array( $this, 'is_user_exists_callback' ),
				'permission_callback' => array( $this, 'pgs_woo_api_permission_callback' ),
			)
		);
	}

	/**
	 * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/delete_account
	 *
	 * @param WP_REST_Request $request Request data.
	 *      @type int    user_id                  User ID.
	 *      @type string user_email               User email.
	 *      @type bool   send_confirmation_email  Send confirmation email.
	 *
	 * @return WP_Error|WP_REST_Response
	 */
	public function delete_account_callback( WP_REST_Request $request ) {
		$params = $request->get_params();
		$data   = array(
			'status' => false,
			'data'   => (object) array(),
		);

		global $pgs_woo_api_delete_account_data;

		$pgs_woo_api_delete_account_data = array(
			'request' => $request,
			'params'  => $params,
		);

		$user_id     = ( isset( $params['user_id'] ) && ! empty( $params['user_id'] ) ) ? absint( wp_unslash( $params['user_id'] ) ) : '';
		$has_user_id = ( isset( $params['user_id'] ) && ! empty( $params['user_id'] ) );

		if ( empty( $user_id ) ) {
			if ( $has_user_id ) {
				$data['message'] = esc_html__( 'Please provide a valid User ID.', 'pgs-woo-api' );
			} else {
				$data['message'] = esc_html__( 'Please provide a User ID.', 'pgs-woo-api' );
			}
			return new WP_REST_Response( $data, 200 );
		}

		$user = get_userdata( $user_id );
		if ( ! $user instanceof WP_User ) {
			$data['message'] = esc_html__( 'Not a valid user.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		$user_email = ( isset( $params['user_email'] ) && ! empty( $params['user_email'] ) ) ? sanitize_email( wp_unslash( $params['user_email'] ) ) : '';

		if ( empty( $user_email ) ) {
			$data['message'] = esc_html__( 'Please provide an email.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		if ( ! is_email( $user_email ) ) {
			$data['message'] = esc_html__( 'Please provide a valid email.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		if ( $user_email !== $user->user_email ) {
			$data['message'] = esc_html__( 'You are not allowed to delete this account.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		$send_confirmation_email  = ( isset( $params['send_confirmation_email'] ) && ! empty( $params['send_confirmation_email'] ) ) ? $params['send_confirmation_email'] : 'yes';
		$send_confirmation_email  = filter_var( $send_confirmation_email, FILTER_VALIDATE_BOOLEAN );
		$action_type              = 'remove_personal_data';
		$send_confirmation_status = $send_confirmation_email ? 'pending' : 'confirmed';

		$request_id = wp_create_user_request( $user_email, $action_type, array(), $send_confirmation_status );
		$message    = '';

		if ( is_wp_error( $request_id ) ) {
			$data['message'] = $request_id->get_error_message();
			return new WP_REST_Response( $data, 200 );
		} elseif ( ! $request_id ) {
			$data['message'] = esc_html__( 'Unable to initiate confirmation request.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		$message = '';
		if ( $send_confirmation_email ) {
			$send_request = wp_send_user_request( $request_id );
			if ( is_wp_error( $send_request ) ) {
				$message = esc_html__( 'Delete account request confirmation initiated successfully, but unable to send the email. Please contact administrator.', 'pgs-woo-api' );
			} else {
				$message = esc_html__( 'Delete account request confirmation initiated successfully and send the email. Please check email.', 'pgs-woo-api' );
			}
		} else {
			$message = esc_html__( 'Delete account request added successfully. Once the request is processed, you will receive a notification via email.', 'pgs-woo-api' );
		}

		$user = get_user_by( 'id', $request['user_id'] );

		$data = array(
			'status'  => true,
			'message' => $message,
			'data'    => (object) array(
				'logout' => ( $send_confirmation_email ) ? 'no' : 'yes',
			),
		);
		return new WP_REST_Response( $data, 200 );
	}

	/**
	 * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/is_user_exists
	 *
	 * @param WP_REST_Request $request Request data.
	 *      @type int    user_id                  User ID.
	 *
	 * @return string
	 */
	public function is_user_exists_callback( WP_REST_Request $request ) {

		$this->wp_rest_request = $request;
		$this->params          = $this->wp_rest_request->get_params();

		if ( isset( $this->params['user_id'] ) && ! empty( $this->params['user_id'] ) ) {
			$this->user_id = absint( wp_unslash( $this->params['user_id'] ) );
		}

		if ( isset( $this->params['app-ver'] ) && ! empty( $this->params['app-ver'] ) ) {
    		$this->app_ver = $this->params['app-ver'];
    	}

		$data = array(
			'is_user_exists' => 'false',
		);

		if ( version_compare( $this->app_ver, '4.4.0', '>=' ) && ! empty( $this->user_id ) ) {
			$is_user_exists = $this->is_user_exists( $this->user_id, $this->params, $this->wp_rest_request );

			$data['is_user_exists'] = $is_user_exists;
		}

		return new WP_REST_Response( $data, 200 );
	}

	/**
	 * Fires once a post has been saved.
	 *
	 * @param int     $post_id Post ID.
	 * @param WP_Post $post    Post object.
	 * @param bool    $update  Whether this is an existing post being updated.
	 * @return void
	 */
	public function save_post( $post_id, $post, $update ) {
		$post_id = absint( $post_id );

		if ( defined( 'REST_REQUEST' ) && REST_REQUEST && 'user_request' === $post->post_type && ! $update ) {
			global $pgs_woo_api_delete_account_data;

			if ( $pgs_woo_api_delete_account_data && ! empty( $pgs_woo_api_delete_account_data ) ) {
				$request = $pgs_woo_api_delete_account_data['request'];
				$params  = $pgs_woo_api_delete_account_data['params'];
				$route   = $request->get_route();

				if ( in_array( 'delete_account', array_filter( explode( '/', $route ) ), true ) ) {
					update_post_meta( $post_id, 'pgs_woo_api_delete_account_request', 'yes' );

					$send_confirmation_email  = ( isset( $params['send_confirmation_email'] ) && ! empty( $params['send_confirmation_email'] ) ) ? $params['send_confirmation_email'] : 'yes';
					$send_confirmation_email  = filter_var( $send_confirmation_email, FILTER_VALIDATE_BOOLEAN );
					if ( ! $send_confirmation_email ) {
						add_user_meta( $post->post_author, 'user_marked_as_deleted', 'yes', true );
					}
				}
			}
		}
	}

	/**
	 * Fires an action hook when the account action has been confirmed by the user.
	 *
	 * @param int $request_id Request ID.
	 * @return void
	 */
	public function user_request_confirmed( $request_id ) {
		$request_post = get_post( $request_id );
		if ( $request_post && 'user_request' === $request_post->post_type ) {
			if (
				isset( $request_post->pgs_woo_api_delete_account_request )
				&& ! empty( $request_post->pgs_woo_api_delete_account_request )
				&& filter_var( $request_post->pgs_woo_api_delete_account_request, FILTER_VALIDATE_BOOLEAN )
			) {
				add_user_meta( $request_post->post_author, 'user_marked_as_deleted', 'yes', true );
			}
		}
	}

	/**
	 * Delete user account once data is removed.
	 *
	 * @param int $request_id The privacy request post ID associated with this request.
	 */
	function after_personal_data_erased( $request_id ) {
		$request = wp_get_user_request( $request_id );

		if ( ! is_a( $request, 'WP_User_Request' ) ) {
			return;
		}

		wp_delete_user( $request->user_id );
	}

	/**
	 * This will add column value in user list table
	 *
	 * @param array  $val value.
	 * @param string $column_name column name.
	 * @param int    $user_id user id.
	 */
	public function users_custom_column( $val, $column_name, $user_id ) {
		// var_dump( $val );
		switch ( $column_name ) {
			case 'cdfs_user_status':
				$status = ( ! empty( $val ) ) ? $val : '&mdash;';
				$user   = get_userdata( $user_id );
				if ( $user instanceof WP_User ) {
					if ( isset( $user->user_marked_as_deleted ) && ! empty( $user->user_marked_as_deleted ) && 'yes' === $user->user_marked_as_deleted ) {
						$status = esc_html__( 'Deleted', 'pgs-woo-api' );
					}
				}
				return $status;
			default:
		}
	}

	/**
	 * Undocumented function
	 *
	 * @param string $description The default description.
	 * @param string $action_name The name of the request.
	 * @return string
	 */
	public function request_action_description( $description, $action_name ) {
		if ( 'remove_personal_data' === $action_name ) {
			global $pgs_woo_api_delete_account_data;
			if ( defined( 'REST_REQUEST' ) && REST_REQUEST && $pgs_woo_api_delete_account_data && ! empty( $pgs_woo_api_delete_account_data ) ) {
				$request = $pgs_woo_api_delete_account_data['request'];
				$route   = $request->get_route();
				if ( in_array( 'delete_account', array_filter( explode( '/', $route ) ), true ) ) {
					$description = esc_html__( 'Delete Account', 'pgs-woo-api' );
				}
			}
		}
		return $description;
	}

	/**
	 * Applying user lock filter on user's authentication
	 *
	 * @param WP_User|WP_Error $user     WP_User or WP_Error object if a previous
	 *                                   callback failed authentication.
	 * @param string           $password Password to check against the user.
	 * @return \WP_Error || $user   If account is marked as deleted return WP_Error object, else return WP_User object.
	 */
	public function check_lock( $user, $password ) {

		if ( is_wp_error( $user ) ) {
			return $user;
		}

		if ( $user instanceof WP_User && isset( $user->ID ) && 'yes' === get_user_meta( (int) $user->ID, 'user_marked_as_deleted', true ) ) {
			return new WP_Error( 'deleted', esc_html__( 'Your username or password is incorrect.', 'pgs-woo-api' ) );
		} else {
			return $user;
		}
	}

	/**
	 * Undocumented function
	 *
	 * @param WP_Error      $errors    A WP_Error object containing any errors generated
	 *                                 by using invalid credentials.
	 * @param WP_User|false $user_data WP_User object if found, false if the user does not exist.
	 * @return void
	 */
	function check_lost_password( $errors, $user_data ) {
		if ( $user_data instanceof WP_User && isset( $user_data->ID ) && 'yes' === get_user_meta( (int) $user_data->ID, 'user_marked_as_deleted', true ) ) {
			$errors->add( 'deleted_account ', 'Invalid username or email.' );
		}
	}
}

new PGS_WOO_API_Account_Controller();
