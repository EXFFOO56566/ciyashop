<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_ReviewsController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'postreview';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {
		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {


        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array( $this, 'pgs_woo_api_get_postreview'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
            'args' => array(
				'product' => array(
					'required' => true,
					'sanitize_callback' => 'absint'
				),
				'comment' => array(
					'required' => true,
					'sanitize_callback' => 'esc_sql'
				),
				'ratestar' => array(
					'required' => true,
					'sanitize_callback' => 'absint'
				),
				'namecustomer' => array(
					'sanitize_callback' => 'esc_sql'
				),
				'emailcustomer' => array(
					'sanitize_callback' => 'esc_sql'
				),
                'user_id' =>  array(
                    'sanitize_callback' => 'esc_sql'
				)
			),
    	) );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/postreview
    * Insert review to product
    * @Param product :  #### add product idid
	* @Param comment : ###
	* @Param ratestar : #### rate star valuew in 1,2 up to 5
	* @Param namecustomer : ###
	* @Param emailcustomer: ###
    * @Param user_id : #### if user logged in
    */
    function pgs_woo_api_get_postreview(WP_REST_Request $request){

        $parameters = $request->get_params();

		$product_id = $parameters['product'];
		$product = wc_get_product($product_id);

        $output = array();
        $error = array( "status" => "error" );

        $reviews_allowed = $product->get_reviews_allowed();
		if($reviews_allowed){
			$is_approved = get_option('comment_moderation');
			$is_whitelist = get_option('comment_whitelist');
			$contentcomment = $parameters['comment'];
			$checkcontent = str_replace(" ","",$contentcomment);
			$lengcontent = strlen($checkcontent);
			$ratestar = $parameters['ratestar'];
			$rs = floatval($ratestar);
			if ( $ratestar < 1 || $ratestar > 5 ) {
				$error['error'] = esc_html__("Please input 1 to 5 in rate star.","pgs-woo-api");
                return $error;
			}
			if ( $lengcontent == 0 ) {
				$error['error'] = esc_html__("Please type a comment.","pgs-woo-api");
                return $error;
			}
			$customer_ip_address = WC_Geolocation::get_ip_address();
			$user_agent = wc_get_user_agent();
			$time = current_time('mysql');
			$user_id = (isset($parameters['user_id'])&&!empty($parameters['user_id']))?$parameters['user_id']:'';
            if(isset($user_id) && !empty($user_id)){
				$user = $user_obj = get_user_by( 'id', $user_id );
				$userid = $user->ID;
				$author = $user->data;
				$author_capabilities = $user->caps;
				if(!empty($author_capabilities['administrator']) && $author_capabilities['administrator'] == true){
					$admins = true;
				}
				if($admins){
					$approved = 1;
				}else{
					if($is_approved == 1){
						$approved = 0;
					}else{
						if($is_whitelist == 1){
							if(!empty($author->user_email)){
								global $wpdb;
								$table = $wpdb->prefix.'comments';
								$checks = $wpdb->get_results("SELECT * FROM $table WHERE comment_author_email = $author->user_email AND comment_approved = 1",OBJECT);
								if(!empty($checks)){
									$approved = 1;
								}else{
									$approved = 0;
								}
							}else{
								$approved = 0;
							}
						}else{
							$approved = 1;
						}
					}
				}
				$idauthor = $author->ID;
				$nameauthor = $author->display_name;
				$emailauthor = $author->user_email;
				$urlauthor = $author->user_url;
				$data = array(
					'comment_post_ID' => $product_id,
					'comment_author' => $nameauthor,
					'comment_author_email' => $emailauthor,
					'comment_author_url' => $urlauthor,
					'comment_content' => $contentcomment,
					'comment_type' => '',
					'comment_karma' => 1,
					'comment_parent' => 0,
					'user_id' => $userid,
					'comment_author_IP' => $customer_ip_address,
					'comment_agent' => $user_agent,
					'comment_date' => $time,
					'comment_approved' => $approved,
					'comment_meta' => array(
						'rating' => $rs,
						'verified' => 1
					)
				);
			} else {
				$validation = new WC_Validation();
				if ( get_option( 'comment_registration' ) ){
					$error['error'] = esc_html__("Sorry, you must be logged in to comment.","pgs-woo-api");
                    return $error;
				}
				if ( get_option('require_name_email') ) {
					$nameauthor = isset($parameters['namecustomer'])? $parameters['namecustomer'] : false;
					$emailauthor = isset($parameters['emailcustomer'])? $parameters['emailcustomer'] : false;
					if ( empty( $nameauthor ) || empty( $emailauthor ) ) {

                        $error['error'] = esc_html__("Creating a comment requires valid author name and email values.","pgs-woo-api");
                        return $error;
					}
					if(!$validation->is_email($emailauthor)){
						$error['error'] = esc_html__("Sorry, $emailauthor is not a valid email.","pgs-woo-api");
                        return $error;
					}
				}
				else{
					$nameauthor = isset($parameters['namecustomer'])? $parameters['namecustomer'] : false;
					$emailauthor = isset($parameters['emailcustomer'])? $parameters['emailcustomer'] : false;
					if ( empty( $nameauthor ) || empty( $emailauthor ) ) {
						$error['error'] = esc_html__("Creating a comment requires valid author name and email values.","pgs-woo-api");
                        return $error;
					}
					if(!$validation->is_email($emailauthor)){
						$error['error'] = esc_html__("Sorry, $emailauthor is not a valid email.","pgs-woo-api");
                        return $error;
                    }
				}
				if($is_approved == 1){
					$approved = 0;
				}else{
					if($is_whitelist == 1){
						if(!empty($emailauthor)){
							global $wpdb;
							$table = $wpdb->prefix.'comments';
							$checks = $wpdb->get_results("SELECT * FROM $table WHERE comment_author_email = '$emailauthor' AND comment_approved = 1",OBJECT);
							if(!empty($checks)){
								$approved = 1;
							}else{
								$approved = 0;
							}
						}else{
							$approved = 0;
						}
					}else{
						$approved = 1;
					}
				}
				$data = array(
					'comment_post_ID' => $product_id,
					'comment_author' => $nameauthor,
					'comment_author_email' => $emailauthor,
					'comment_author_url' => 'http://',
					'comment_content' => $contentcomment,
					'comment_type' => '',
					'comment_parent' => 0,
					'comment_karma' => 1,
					'user_id' => '0',
					'comment_author_IP' => $customer_ip_address,
					'comment_agent' => $user_agent,
					'comment_date' => $time,
					'comment_approved' => $approved,
					'comment_meta' => array(
						'rating' => $rs,
						'verified' => 0
					)
				);
			}
			wp_insert_comment($data);
			return array(
				'status' => 'success',
				'message' => esc_html__( 'Comment added successfully.','pgs-woo-api' )
			);

		}else{
			return array(
				'status' => 'error',
				'message' => esc_html__( 'Products are not allowed to comment.','pgs-woo-api' )
			);
		}
    }
 }
 new PGS_WOO_API_ReviewsController; ?>