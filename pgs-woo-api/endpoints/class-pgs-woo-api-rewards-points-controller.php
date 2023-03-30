<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_RewardsPointsController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'rewardspoints';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_get_rewardspoints'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/rewardspoints
    * @param user_id : ####
    * @param page : ####
    */
    public function pgs_woo_api_get_rewardspoints( WP_REST_Request $request){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'user_id','page' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }


        global $wc_points_rewards;
        $user_id = $request['user_id'];
        $current_page = $request['page'];
		$points_balance = WC_Points_Rewards_Manager::get_users_points( $user_id );
		$points_label   = $wc_points_rewards->get_points_label( $points_balance );

		$count        = apply_filters( 'wc_points_rewards_my_account_points_events', 5, $user_id );
		$current_page = empty( $current_page ) ? 1 : absint( $current_page );

		// get a set of points events, ordered newest to oldest
		$args = array(
			'calc_found_rows' => true,
			'orderby' => array(
				'field' => 'date',
				'order' => 'DESC',
			),
			'per_page' => $count,
			'paged'    => $current_page,
			'user'     => $user_id,
		);

		$events = WC_Points_Rewards_Points_Log::get_points_log_entries( $args );
		$total_rows = WC_Points_Rewards_Points_Log::$found_rows;

        // load the template
        if(isset($total_rows) && $total_rows > 0){
        	$data = array(
        		'points_balance' => $points_balance,
        		'points_label'   => $points_label,
        		'events'         => $events,
        		'total_rows'     => $total_rows,
        		'current_page'   => $current_page,
        		'count'          => $count,
        	);
            $result = array(
                'status' => 'success',
                'data' => $data
            );
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
new PGS_WOO_API_RewardsPointsController;