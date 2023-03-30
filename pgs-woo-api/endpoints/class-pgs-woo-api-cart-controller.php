<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_CartController extends  PGS_WOO_API_Controller{
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
	protected $rest_base = 'add_to_cart';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_add_to_cart'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/add_to_cart
    * @param user_id: ####
    * @param cart_items: cart items array
    */
    public function pgs_woo_api_add_to_cart(){
        // clear current cart, incase you want to replace cart contents, else skip this step
        WC()->cart->empty_cart();

        $output =  array();
        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        if(!isset($request['cart_items'])){
            $returnArr['status'] = 'error';
        	$returnArr['message'] = esc_html__( 'Sorry, that is not valid input. You missed cart_items parameters','pgs-woo-api' );
        	return $returnArr;
        }

        $cart_items = $request['cart_items'];
        if(isset($cart_items)){
            if(!empty($cart_items)){

                $output =  array(
                    "status" => "success",
                    "checkout_url" => pgs_woo_api_get_app_checkout_page(),
                    "thankyou" => wc_get_endpoint_url( 'order-received', '', wc_get_page_permalink( 'checkout' ) ),
                    "thankyou_endpoint" => pgs_woo_api_get_app_thankyou_page_endpoint(),
                    "home_url" => esc_url(home_url('/'))
                );
                return $output;

            } else {
                $returnArr['status'] = 'error';
            	$returnArr['message'] = esc_html__( 'Cart is empty','pgs-woo-api' );
            	return $returnArr;
            }
        }
        $output =  array(
            "status" => "success",
            "checkout_url" => pgs_woo_api_get_app_checkout_page(),
            "thankyou_endpoint" => pgs_woo_api_get_app_thankyou_page_endpoint(),
            "thankyou" => site_url('checkout/order-received')
        );
        return $output;
    }
 }
 new PGS_WOO_API_CartController;
