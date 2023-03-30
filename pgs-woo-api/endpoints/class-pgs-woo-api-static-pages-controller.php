<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_StaticPagesController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'static_page';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this, 'pgs_woo_api_static_page'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );


    }
    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/static_page
    * @param page:####
    */
    public function pgs_woo_api_static_page( WP_REST_Request $request){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);

        $required = array( 'page' );
        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation) {
           return $validation;
        }



        $error = array( "status" => "error" );
        $output = array();$titles = array();$content='';

        if(isset($request['page']) && !empty($request['page'])){
            $static_pages = array();
            $static_pages = get_option('pgs_woo_api_home_option');

            $key = trim(sanitize_text_field($request['page']));
            if(isset($static_pages['static_page'][$key])){
                $postid = $static_pages['static_page'][$key];//This is page id or post id
                $lang='';
                $is_wpml_active = pgs_woo_api_is_wpml_active();
                if($is_wpml_active){
                    $lang = pgs_woo_api_wpml_get_lang();
                    if(!empty($lang)){
                        if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) ) {
                            if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) ) {
                                $id = icl_object_id($postid, 'post', true,$lang);
                                $postid = $id;
                            }
                        }
                    }
                }
                if(isset($postid) && !empty($postid)){
                    $content_post = get_post($postid);
                    $content = $content_post->post_content;
                    $content = apply_filters('the_content', $content);
                    $content = str_replace(']]>', ']]&gt;', $content);
                    //$content = do_shortcode($content);
                    $output =  array(
                        "status" => "success",
                        "data" => $content
                    );
                } else {
                    $error['message'] = esc_html__("page not set","pgs-woo-api");
                    return $error;
                }
            } else {
                $error['message'] = esc_html__("invalid page name","pgs-woo-api");
                return $error;
            }
        } else {
            $error['message'] = esc_html__("Please pass static page name","pgs-woo-api");
            return $error;
        }
        return $output;
    }
 }
new PGS_WOO_API_StaticPagesController;