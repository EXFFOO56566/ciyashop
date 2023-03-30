<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_Products_New_Controller extends PGS_WOO_API_Controller{
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
	//protected $rest_base = 'products_new';
	private $is_currency_switcher_active = false;
    private $is_yith_featured_video_active = false;

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {
		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {
        register_rest_route( $this->namespace, 'products_sale', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_get_products_sale'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
		register_rest_route( $this->namespace, 'products_random', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_get_random'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/products_sale
    */
    public function pgs_woo_api_get_products_sale(WP_REST_Request $request){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

		pgs_woo_api_set_currency();

		$scrolling_product = new PGS_WOO_API_ScrollingController();
		$data = $scrolling_product->pgs_woo_api_get_scrolling_sale_product($request);
        return $data;
    }



    public function pgs_woo_api_get_random(WP_REST_Request $request){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

		pgs_woo_api_set_currency();

		$scrolling_product = new PGS_WOO_API_ScrollingController();
        $data = $scrolling_product->pgs_woo_api_get_scrolling_random_product($request);
        return $data;
	}

 }
new PGS_WOO_API_Products_New_Controller();
