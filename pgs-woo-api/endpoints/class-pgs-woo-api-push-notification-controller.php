<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_PushNotificationsController extends PGS_WOO_API_Controller{
	/**
	 * Manage Pusnotification Log
     * Endpoint namespace.
	 *
	 * @var string
	 */
	protected $namespace = 'pgs-woo-api/v1';

    protected $push_table = "pgs_woo_api_notifications";
    protected $push_meta_table = "pgs_woo_api_notifications_meta";
    protected $push_relation_table = "pgs_woo_api_notifications_relationships";
	/**
	 * Route base.
	 *
	 * @var string
	 */
	protected $rest_base = 'push_notifications';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_push_notifications_log'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'add_notifications', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_add_notifications_data'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'delete_notifications', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_delete_push_notifications_log'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'notifications_status', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_set_notifications_status_with_token'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/add_notifications
    * @param device_token : ####
    * @param device_type : ####
    */
    public function pgs_woo_api_add_notifications_data( WP_REST_Request $request){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'device_token','device_type' );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }


        $error = array( "status" => "error" );
        $output = array();$titles = array();$content='';

        if(isset($request['device_token']) && !empty($request['device_token'])){
            $user_id = (isset($request['user_id']))? $request['user_id'] : 0;
            pgs_woo_api_add_push_notification_data($request['device_token'],$request['device_type'],$user_id);
            $output =  array(
                "status" => "success",
                "message" => esc_html__("Notification added successfully","pgs-woo-api")
            );
        } else {
            $error['message'] = esc_html__("Something went wrong","pgs-woo-api");
            return $error;
        }
        return $output;
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/push_notifications
    * @param page:####
    */
    public function pgs_woo_api_push_notifications_log( WP_REST_Request $request){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'device_token' );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }


        $error = array( "status" => "error" );
        $output = array();$titles = array();$content='';

        if(isset($request['device_token']) && !empty($request['device_token'])){
            global $wpdb;
            $push_table = $wpdb->prefix . $this->push_table;
            $push_meta = $wpdb->prefix . $this->push_meta_table;
            $push_relation_table = $wpdb->prefix . $this->push_relation_table;

            $token = $request['device_token'];

            $qur = "SELECT $push_table.*,$push_meta.id as push_meta_id,$push_meta.msg,$push_meta.custom_msg,$push_meta.not_code
            FROM $push_table
            INNER JOIN $push_relation_table ON $push_relation_table.not_id = $push_table.id
            INNER JOIN $push_meta ON $push_meta.id = $push_relation_table.push_meta_id
            WHERE device_token = '$token'";
            $results = $wpdb->get_results( $qur, ARRAY_A );
            if(!empty($results)){
                $output =  array(
                    "status" => "success",
                    "data" => $results
                );
            } else {
                $error['message'] = esc_html__("No data found","pgs-woo-api");
                return $error;
            }
        } else {
            $error['message'] = esc_html__("Something went wrong","pgs-woo-api");
            return $error;
        }
        return $output;
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/delete_notifications
    * @param push_meta_id:####
    */
    public function pgs_woo_api_delete_push_notifications_log( WP_REST_Request $request){
        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'push_meta_id' );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }


        $error = array( "status" => "error" );
        $output = array();$titles = array();$content='';

        if(isset($request['push_meta_id']) && !empty($request['push_meta_id'])){
            global $wpdb;

            $push_meta_ids = $request['push_meta_id'];
            $push_meta = $wpdb->prefix . $this->push_meta_table;

            $ids = implode( ',', $push_meta_ids );
            $is_delete = $wpdb->query( "DELETE FROM $push_meta WHERE id IN($ids)" );
            if($is_delete){
                $output =  array(
                    "status" => "success",
                    "message" => esc_html__("Record deleted successfully","pgs-woo-api")
                );
            } else {
                $error['message'] = esc_html__("Record cannot be deleted. Please try after sometime!","pgs-woo-api");
                return $error;
            }
        } else {
            $error['message'] = esc_html__("Something went wrong!","pgs-woo-api");
            return $error;
        }
        return $output;
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/notifications_status
    * @param device_token:####
    * @param device_type:####
    * @param status:####
    */
    public function pgs_woo_api_set_notifications_status_with_token( WP_REST_Request $request){
        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'device_token','device_type','status' );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }


        $error = array( "status" => "error" );
        $output = array();

        if(isset($request['device_token']) && !empty($request['device_token'])){
            $status = (!empty($request['status']))?$request['status']:0;
            pgs_woo_api_add_push_notification_data($request['device_token'],$request['device_type'],0,$status);
            $output =  array(
                "status" => "success",
                "message" => esc_html__("Notification status updated successfully","pgs-woo-api")
            );
        } else {
            $error['message'] = esc_html__("Something went wrong","pgs-woo-api");
            return $error;
        }
        return $output;
    }
 }
new PGS_WOO_API_PushNotificationsController;