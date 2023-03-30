<?php
/**
 * PGS Woo API Delivery Boy Controller Class
 *
 * @link       http://www.potenzaglobalsolutions.com/
 * @since      4.0.0
 *
 * @package    PGS Woo API
 * @subpackage PGS Woo API/endpoints
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

/**
 * PGS_WOO_API_DeliveryBoy_Controller class.
 *
 * @since      4.0.0
 * @package    PGSDB
 * @subpackage PGSDB/endpoints
 * @author     Potenza Global Solutions <wp@potenzaglobalsolutions.com>
 */
class PGS_WOO_API_DeliveryBoy_Controller extends PGS_WOO_API_Controller {

	/**
	 * Endpoint namespace.
	 *
	 * @var string
	 */
	protected $namespace = 'pgs-woo-api/v1';

	/**
	 * Rest API Route Base
	 *
	 * @since  4.0.0
	 * @access protected
	 * @var    string $rest_base  The route base.
	 */
	protected $rest_base = 'get_db_location';

	/**
	 * Initialize the class and set its properties.
	 *
	 * @since    4.0.0
	 */
	public function __construct() {
		add_action( 'rest_api_init', array( $this, 'register_routes' ) );
	}

	/**
	 * Register the routes for this class
	 *
	 * POST /get_db_location
	 *
	 * @since 4.0.0
	 */
	public function register_routes() {

		register_rest_route(
			$this->namespace,
			$this->rest_base,
			array(
				'methods'             => WP_REST_Server::CREATABLE,//'POST',
				'callback'            => array( $this, 'get_delivery_boy_location' ),
				'permission_callback' => array( $this, 'pgs_woo_api_permission_callback' ),
			)
		);

	}

	/**
	 * Set delivery boy location.
	 *
	 * URL : https://your-website/wp-json/pgs-woo-api/v1/get_db_location
	 *
	 * @param WP_REST_Request $request {
	 *     	 Request used to generate the response.
	 *
	 *       @type int|string $user_id    User ID.
	 *       @type int|string $order_id   Order ID.
	 * }
	 * @return WP_Error|WP_REST_Response
	 */
	public function get_delivery_boy_location( WP_REST_Request $request ) {
		$params  = $request->get_params();
		$data    = array(
			'status' => 'error',
		);

		$user_id  = (int) ( ( isset( $params['user_id'] ) && ! empty( $params['user_id'] ) ) ? sanitize_text_field( wp_unslash( $params['user_id'] ) ) : '' );
		$order_id = (int) ( ( isset( $params['order_id'] ) && ! empty( $params['order_id'] ) ) ? sanitize_text_field( wp_unslash( $params['order_id'] ) ) : '' );

		if ( isset( $params['user_id'] ) && 0 === $user_id ) {
			$data['message'] = esc_html__( 'The user ID is empty. Please provide a user ID.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		if ( isset( $params['order_id'] ) && 0 === $order_id ) {
			$data['message'] = esc_html__( 'The order ID is empty. Please provide a order ID.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		if ( 0 === $user_id ) {
			$data['message'] = esc_html__( 'Invalid user ID. Please try later.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		$user = get_userdata( $user_id );

		if ( ! $user ) {
			$data['message'] = esc_html__( 'User not found. Please try later.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		if ( 0 === $order_id ) {
			$data['message'] = esc_html__( 'Invalid order ID. Please try later.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		// Get an instance of the WC_Order object (same as before).
		$order = wc_get_order( $order_id );

		if ( ! $order ) {
			$data['message'] = esc_html__( 'Order not found. Please try later.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		$order_status = $order->get_status();
		if ( 'db_assigned' !== $order_status ) {
			$data['message'] = esc_html__( 'Invalid order status. Please contact the system administrator.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		$customer_id = $order->get_customer_id();
		if ( (int) $customer_id !== (int) $user_id ) {
			$data['message'] = sprintf(
				esc_html__( 'The order customer does not match. Please contact the system administrator. (Order #:%1$s)', 'pgs-woo-api' ),
				esc_attr( $order_id )
			);
			return new WP_REST_Response( $data, 200 );
		}

		// Update order status.
		$lat  = get_post_meta( $order_id, 'pgsdb_order_delivery_boy_location_lat', true );
		$long = get_post_meta( $order_id, 'pgsdb_order_delivery_boy_location_long', true );

		if ( ! $lat || ! $long ) {
			$data['message'] = esc_html__( 'Unable to get location. Please try later.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		$data = array(
			'status'  => 'success',
			'message' => esc_html__( 'Location received successfully.', 'pgs-woo-api' ),
			'data'    => array(
				'lat'  => $lat,
				'long' => $long,
			),
		);

		return new WP_REST_Response( $data, 200 );
	}

}

new PGS_WOO_API_DeliveryBoy_Controller();
