<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_GeoFencingController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'findGeolocation';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_find_geolocation'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/findGeolocation
    * @param lat : ####
    * @param lng : ####
    * @param device_token : ####
    * @param device_type : ####
    */
    public function pgs_woo_api_find_geolocation( WP_REST_Request $request){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'lat','lng','device_token','device_type' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }

        $output = array();
        $error = array( "status" => "error" );


        $lat = $request['lat'];
        $lng = $request['lng'];

        $result = array(
            'status' => 'error',
            'msg' => esc_html__( 'Something went wrong!','pgs-woo-api')
        );


        global $wpdb;
        $geo_fencinge = $wpdb->prefix . "pgs_woo_api_geo_fencing";
        $posts_tbl = $wpdb->prefix . "posts";

        $data = array(); $content = '';
        $query = "SELECT $geo_fencinge.id,$geo_fencinge.post_id,
        ( 3959 * acos( cos( radians($lat) ) * cos( radians( lat ) ) * cos( radians( lng ) - radians($lng) ) + sin( radians($lat) ) * sin( radians( lat ) ) ) ) * 1609.34 AS distance
        FROM $geo_fencinge
        INNER JOIN $posts_tbl ON $posts_tbl.ID = $geo_fencinge.post_id
        WHERE $posts_tbl.post_status = 'publish'
        AND (( 3959 * acos( cos( radians($lat) ) * cos( radians( lat ) ) * cos( radians( lng ) - radians($lng) ) + sin( radians($lat) ) * sin( radians( lat ) ) ) ) * 1609.34 ) <= $geo_fencinge.radius ORDER BY distance LIMIT 1";
        $data = $wpdb->get_results( $query, OBJECT );
        if(!empty($data)){
            $geo_location = get_post_meta($data[0]->post_id,'geo_location',true);
            $geo_title = get_the_title($data[0]->post_id);
            $content_post = get_post($data[0]->post_id);
            $content = $content_post->post_content;
            $data = array(
                "geo_title" => $geo_title,
                "geo_content" => $content,
                "geo_location" => $geo_location
                //"distance" => $data[0]->distance
            );
            $result = array(
                'status' => 'success',
                'data' => $data
            );
            $push = new PGS_WOO_API_Controller;
            $notification_code = 3;
            $device_data[] = array(
                'token' => $request['device_token'],
                'type' => $request['device_type']
            );
            if(isset($request['device_token'])){
                $message = $content;
                $msg = $geo_title;
                $custom_msg = $message;
                $badge = 0;
                $push->send_push( $msg, $badge, $custom_msg,$notification_code,$device_data);
            }
        } else {
            $result = array(
                'status' => 'success',
                'msg' => esc_html__( 'Result not found!','pgs-woo-api')
            );
        }

        echo wp_json_encode($result);
        exit();
    }
 }
new PGS_WOO_API_GeoFencingController;