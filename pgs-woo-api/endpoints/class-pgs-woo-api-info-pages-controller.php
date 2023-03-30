<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_InfoPagesController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'info_pages';

	protected $wallet_provider = 'woo-wallet';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_app_info_pages'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );


    }
    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/info_pages
    *
    */
    public function pgs_woo_api_app_info_pages( $request ){
		$params = $request->get_params();

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $error = array( "status" => "error" );
        $output = array();$titles = array();$content='';
        $info_pages = array();

        if(isset($request['page_id']) && !empty($request['page_id'])){

            $postid = $request['page_id'];//This is page id or post id
            $content_post = get_post($postid);
            $content = $content_post->post_content;
            $content = apply_filters('the_content', $content);
            $content = str_replace(']]>', ']]&gt;', $content);

            $output =  array(
                "status" => "success",
                "data" => $content
            );
        } else {
            $info_pages = get_option('pgs_woo_api_home_option');
            if(isset($info_pages['info_pages']) && !empty($info_pages['info_pages'])){

                foreach($info_pages['info_pages'] as $key => $val){

                    if(isset($val['info_pages_page_id']) && !empty($val['info_pages_page_id']) ){
                        $titles[] = array(
                            'title' => get_the_title($val['info_pages_page_id']),
                            'page_id' => $val['info_pages_page_id']
                        );
                    }

                }
                $output =  array(
                    "status" => "success",
                    "data" => $titles
                );
            } else {
                $error['message'] = esc_html__("No data found!","pgs-woo-api");
                return $error;
            }

        }

		// Integrate Wallet Data
		$wallet_default_data = array(
			'user_id'       => $params['user_id'],
			'provider'      => $this->wallet_provider,
			'balance'       => '0.00',
			'currency'      => get_woocommerce_currency_symbol(),
		);

		$wallet_data = array();

		if( isset( $params['user_id'] ) ) {
			// Retrieve user info by user ID.
			$user = get_userdata( $params['user_id'] );

			if ( $user ) {
				if( $this->wallet_provider == 'woo-wallet' && function_exists( 'woo_wallet' ) ) {
					$wallet_data = array(
						'balance'       => woo_wallet()->wallet->get_wallet_balance( $params['user_id'], 'edit' ),
						'currency'      => get_woocommerce_currency_symbol(),
					);
				}
			}
		}
		$wallet_data = wp_parse_args( $wallet_data, $wallet_default_data );

		$output = array_merge( $output, $wallet_data );

        return $output;
    }
 }
new PGS_WOO_API_InfoPagesController;
