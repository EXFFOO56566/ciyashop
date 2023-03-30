<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_StoreFinderController extends  PGS_WOO_API_Controller{
	/**
	 * Endpoint namespace.
	 *
	 */
	protected $namespace = 'pgs-woo-api/v1';

	/**
	 * Route base.
	 */
	protected $rest_base = 'storefinder';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {
		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}

	public function pgs_woo_api_register_route() {
        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_find_store' ),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/storefinder
    * @param category slug: ####
    */
    public function pgs_woo_api_find_store(){
        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
        $pgs_api_woo_storefinder = (isset($pgs_woo_api_home_option['storefinder']))?$pgs_woo_api_home_option['storefinder']:array();
        $storefinder = array();
        if(!empty($pgs_api_woo_storefinder)){
            $i = 1;
            foreach( $pgs_api_woo_storefinder as $store){
                $storefinder[] = array(
                    'id' => $i,
                    'address' => $store['address'],
                    'lat' => $store['lat'],
                    'lng' => $store['lng']
                );
                $i++;
            }
            $data = array(
                'status' => 'success',
                'data' => $storefinder,
            );
        } else {
            $data = array(
                'status' => 'error',
                'data' => $storefinder,

            );
        }
        return $data;
    }
 }
 new PGS_WOO_API_StoreFinderController;