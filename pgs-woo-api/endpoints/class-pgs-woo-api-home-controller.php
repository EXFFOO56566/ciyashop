<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_HomeController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'home';

    /**
	 * Currency switcher active status.
	 *
	 * @var bool
	 */
	private $is_currency_switcher_active = false;

	/**
	 * YITH Featured Video active status.
	 *
	 * @var bool
	 */
    private $is_yith_featured_video_active = false;

	/**
	 * PGS Woo API version.
	 *
	 * @var string
	 */
    private $app_ver = '';

	/**
	 * WP_REST_Request object.
	 *
	 * @var WP_REST_Request
	 */
    private $wp_rest_request = null;

	/**
	 * Request params.
	 *
	 * @var array
	 */
    private $params = array();

	/**
	 * User ID.
	 *
	 * @var int
	 */
    private $user_id = '';

	public function __construct() {
		$this->register_routes();
	}

	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}

	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
			'methods'             => WP_REST_Server::CREATABLE,//'POST',
			'callback'            => array($this, 'pgs_woo_api_app_home'),
			'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
        ) );

        register_rest_route( $this->namespace, 'home_layout', array(
			'methods'             => WP_REST_Server::CREATABLE,//'POST',
			'callback'            => array($this, 'pgs_woo_api_get_app_home_layout'),
			'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
        ) );

        register_rest_route( $this->namespace, 'home_scrolling', array(
			'methods'             => WP_REST_Server::CREATABLE,//'POST',
			'callback'            => array($this, 'pgs_woo_api_get_app_home_scrolling'),
			'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

		register_rest_route(
			$this->namespace,
			'verified',
			array(
				'methods'             => WP_REST_Server::CREATABLE, // 'POST',
				'callback'            => array( $this, 'set_verificaion_status' ),
				'permission_callback' => array( $this, 'pgs_woo_api_permission_callback' ),
			)
		);

    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/home_scrolling
    * app-ver : ####
    */
    public function pgs_woo_api_get_app_home_scrolling( WP_REST_Request $request ) {

        $this->wp_rest_request = $request;
		$this->params          = $this->wp_rest_request->get_params();

		if ( isset( $this->params['user_id'] ) && ! empty( $this->params['user_id'] ) ) {
			$this->user_id = absint( $this->params['user_id'] );
		}

		if ( isset( $this->params['app-ver'] ) && ! empty( $this->params['app-ver'] ) ) {
    		$this->app_ver = $this->params['app-ver'];
    	}

		$input   = file_get_contents( "php://input" );
        $request = json_decode( $input, true );

		pgs_woo_api_set_currency();

        /**
         * Check currency switcher plugin is active
         */
        $this->is_currency_switcher_active = pgs_woo_api_is_currency_switcher_active();

        /**
         * Check yith featured video plugin is active
         */
        $this->is_yith_featured_video_active = pgs_woo_api_is_yith_featured_video_active();//cehck plugin active


        $lang='';
        $is_wpml_active = pgs_woo_api_is_wpml_active();
        if($is_wpml_active){
            $lang = pgs_woo_api_wpml_get_lang();
            if(!empty($lang)){
                $lang_prifix = '_'.$lang;
                $pgs_woo_api_wpml_home_option = get_option('pgs_woo_api_home_option'.$lang_prifix);
            }
        }

        $pgs_woo_api_home_option = array();$pgs_woo_api_home_option['app_logo'] = '';$pgs_woo_api_home_option['app_logo_light'] = '';
        $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');

        $price_formate_option = get_woo_price_formate_option_array();

        if(!empty($lang)){
            $pgs_woo_api_home_option['app_logo'] = $this->pgs_woo_api_get_app_logo($pgs_woo_api_wpml_home_option,$pgs_woo_api_home_option);
            $pgs_woo_api_home_option['app_logo_light'] = $this->pgs_woo_api_get_app_logo_light($pgs_woo_api_wpml_home_option,$pgs_woo_api_home_option);
            $pgs_woo_api_home_option['main_category'] = $this->pgs_woo_api_get_main_category($pgs_woo_api_home_option,$lang);
        } else {
            $pgs_woo_api_home_option['app_logo'] = $this->pgs_woo_api_get_app_logo($pgs_woo_api_home_option);
            $pgs_woo_api_home_option['app_logo_light'] = $this->pgs_woo_api_get_app_logo_light($pgs_woo_api_home_option);
            $pgs_woo_api_home_option['main_category'] = $this->pgs_woo_api_get_main_category($pgs_woo_api_home_option);
        }

        if($this->app_ver !== ''){

        } else {
            unset($pgs_woo_api_home_option['products_carousel']);
        }
        $pgs_woo_api_home_option['static_page'] = $this->pgs_woo_api_get_static_pages($pgs_woo_api_home_option,$lang);
        $pgs_woo_api_home_option['info_pages'] = $this->pgs_woo_api_get_info_pages($pgs_woo_api_home_option,$lang);
        $pgs_woo_api_home_option['all_categories'] = $this->pgs_woo_api_cat_list();
        $pgs_woo_api_home_option['is_wishlist_active'] = pgs_woo_api_is_wishlist_active();
        $pgs_woo_api_home_option['is_currency_switcher_active'] = $this->is_currency_switcher_active;
        $pgs_woo_api_home_option['is_yith_featured_video_active'] = $this->is_yith_featured_video_active;
        $pgs_woo_api_home_option['is_order_tracking_active'] = pgs_woo_api_is_order_tracking_active();
        $pgs_woo_api_home_option['is_reward_points_active'] = pgs_woo_api_is_reward_points_active();
        $pgs_woo_api_home_option['is_guest_checkout_active'] = pgs_woo_api_is_guest_checkout();
        $is_wpml_active = pgs_woo_api_is_wpml_active();
        if($is_wpml_active){
            $pgs_api_is_wpml_status = (isset($pgs_woo_api_home_option['pgs_api_is_wpml']))?$pgs_woo_api_home_option['pgs_api_is_wpml']:'enable';
            if($pgs_api_is_wpml_status == "enable"){
                $is_wpml_active = true;
            } else {
                $is_wpml_active = false;
            }
        }

        $pgs_woo_api_home_option['is_wpml_active'] = $is_wpml_active;
        $pgs_woo_api_home_option['price_formate_options'] = $price_formate_option;

        $pgsiosappurl = get_option('pgs_ios_app_url');
        $pgs_ios_app_url = (isset($pgsiosappurl))?$pgsiosappurl:'';
        $pgs_woo_api_home_option['ios_app_url'] = $pgs_ios_app_url;
        $site_language = get_bloginfo('language');
        $pgs_woo_api_home_option['site_language'] = $site_language;
        $pgs_woo_api_home_option['wpml_languages'] = $this->pgs_woo_api_get_all_wpml_langs($is_wpml_active);
        $checkout_redirect_urls  = $this->pgs_woo_api_get_checkout_redirect_url();
        $pgs_woo_api_home_option['checkout_redirect_url'] = $checkout_redirect_urls;
        $pgs_woo_api_home_option['pgs_app_contact_info'] = $this->pgs_woo_api_get_contact_info($pgs_woo_api_home_option);

        /**
         *  Get App Assets app color
         */
        $app_color = array(
            'header_color' => '',
            'primary_color' => '#60A727',
            'secondary_color' => ''
        );
        $app_assets = get_option('pgs_woo_api_app_assets_options');
        if(isset($app_assets) && !empty($app_assets)){
            if(isset($app_assets['app_assets']['app_color']) && !empty($app_assets['app_assets']['app_color'])){
                $app_color = $app_assets['app_assets']['app_color'];
                if ( version_compare( $this->app_ver, '4.0', '>=' ) ) {
                    $app_color['header_color'] = $app_assets['app_assets']['app_color']['primary_color'];
                } else {
                    $header_color = $app_assets['app_assets']['app_color']['primary_color'];
                    if ( isset( $app_assets['app_assets']['app_color']['header_color'] ) && ! empty( $app_assets['app_assets']['app_color']['header_color'] ) ) {
                        $header_color = $app_assets['app_assets']['app_color']['header_color'];
                    }
                    $app_color['header_color'] = $header_color;
                }
            }
        }

        $pgs_woo_api_home_option['app_color'] = $app_color;
        $pgs_woo_api_home_option['wc_tax_enabled'] = false;//wc_tax_enabled
        if(wc_tax_enabled()){
            $pgs_woo_api_home_option['wc_tax_enabled'] = true;
        }
        $pgs_woo_api_home_option['woocommerce_tax_display_shop'] = get_option( 'woocommerce_tax_display_shop' );
        $pgs_woo_api_home_option['woocommerce_tax_display_cart'] = get_option( 'woocommerce_tax_display_cart' );
        $pgs_woo_api_home_option['woocommerce_price_display_suffix'] = get_option( 'woocommerce_price_display_suffix' );
        $pgs_woo_api_home_option['woocommerce_tax_total_display'] = get_option( 'woocommerce_tax_total_display' );

        /**
         *
         */
        $scrolling_product = new PGS_WOO_API_ScrollingController();
        $pgs_woo_api_home_option['products_random'] = $scrolling_product->pgs_woo_api_get_scrolling_random_product($request);

        $is_rtl = false;
        if ( is_rtl() ) {
            $is_rtl = true;
        }
        $pgs_woo_api_home_option['is_rtl'] = $is_rtl;
        if($this->is_currency_switcher_active){
            $currency_data = get_option('woocs');
            if(isset($currency_data) && !empty($currency_data)){
				global $WOOCS;
				if( $WOOCS ) {
					$currencies = $WOOCS->get_currencies();
					if(isset($currencies) && !empty($currencies)){
						$pgs_woo_api_home_option['currency_switcher'] = $currencies;
					}
				}
            }
        }
        $data = false;
        if(get_option('app_validation')){
            $data = true;
        }
        $pgs_woo_api_home_option['is_app_validation'] = $data;


        $is_login_val = (isset($pgs_woo_api_home_option['pgs_woo_api_scroll_is_login']) && !empty($pgs_woo_api_home_option['pgs_woo_api_scroll_is_login']))?$pgs_woo_api_home_option['pgs_woo_api_scroll_is_login']:'enable';
        $is_slider_val = (isset($pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider']) && !empty($pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider']))?$pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider']:'enable';
        $is_login = ($is_login_val == 'enable')?true:false;
        $is_slider = ($is_slider_val == 'enable')?true:false;
        $pgs_woo_api_home_option['is_login'] = $is_login;
        $pgs_woo_api_home_option['is_slider'] = $is_slider;

		$pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option'] = (isset($pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']) && !empty($pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']))?$pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']:'enable';
		$pgs_woo_api_home_option['pgs_woo_api_deliver_pincode'] = $this->pgs_woo_api_is_pincode_plugin();
		$pgs_woo_api_home_option['pgs_woo_api_web_view_pages'] = $this->pgs_woo_api_app_get_web_view_page_list();

        unset($pgs_woo_api_home_option['pgs_woo_api_scroll_is_login']);
        unset($pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider']);
		unset($pgs_woo_api_home_option['category_banners']);
		unset($pgs_woo_api_home_option['products_carousel']);
		unset($pgs_woo_api_home_option['banner_ad']);
		unset($pgs_woo_api_home_option['feature_box_status']);
		unset($pgs_woo_api_home_option['feature_box_heading']);
		unset($pgs_woo_api_home_option['feature_box']);
		unset($pgs_woo_api_home_option['products_view_orders']);
		//review varification
		$pgs_woo_api_home_option['woocommerce_review_rating_verification_required'] =get_option( 'woocommerce_review_rating_verification_required' );
		$pgs_woo_api_home_option['woocommerce_enable_reviews'] =get_option( 'woocommerce_enable_reviews' );

		$pgs_woo_api_home_option['is_user_exists'] = 'not_provided';
		if ( version_compare( $this->app_ver, '4.4.0', '>=' ) && ! empty( $this->user_id ) ) {
			$is_user_exists = $this->is_user_exists( $this->user_id, $this->params, $this->wp_rest_request );

			$pgs_woo_api_home_option['is_user_exists'] = $is_user_exists;
		}

        return $pgs_woo_api_home_option;
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/home_layout
    * app-ver : ####
    */
    public function pgs_woo_api_get_app_home_layout( WP_REST_Request $request ){

		$this->wp_rest_request = $request;
		$this->params          = $this->wp_rest_request->get_params();

		if ( isset( $this->params['user_id'] ) && ! empty( $this->params['user_id'] ) ) {
			$this->user_id = absint( $this->params['user_id'] );
		}

		if ( isset( $this->params['app-ver'] ) && ! empty( $this->params['app-ver'] ) ) {
    		$this->app_ver = $this->params['app-ver'];
    	}

        $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
        $home_layout             = ( isset( $pgs_woo_api_home_option['pgs_woo_api_home_layout'] ) && ! empty( $pgs_woo_api_home_option['pgs_woo_api_home_layout'] ) ) ? $pgs_woo_api_home_option['pgs_woo_api_home_layout'] : 'default';
        $is_login_val            = ( isset( $pgs_woo_api_home_option['pgs_woo_api_scroll_is_login'] ) && ! empty( $pgs_woo_api_home_option['pgs_woo_api_scroll_is_login'] ) ) ? $pgs_woo_api_home_option['pgs_woo_api_scroll_is_login'] : 'disable';
        $is_slider_val           = ( isset( $pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider'] ) && ! empty( $pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider'] ) ) ? $pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider'] : 'disable';
        $is_login                = ( $is_login_val == 'enable' ) ? true : false;
        $is_slider               = ( $is_slider_val == 'enable' ) ? true : false;
		$is_rtl                  = false;

        if ( is_rtl() ) {
            $is_rtl = true;
        }

		$lang           = '';
        $is_wpml_active = pgs_woo_api_is_wpml_active();

		if ( $is_wpml_active ) {
            $lang = pgs_woo_api_wpml_get_lang();
            if ( ! empty( $lang ) ) {
                $lang_prifix                  = '_'.$lang;
                $pgs_woo_api_wpml_home_option = get_option('pgs_woo_api_home_option'.$lang_prifix);
            }
        }

        $responce = array(
            'home_layout'              => $home_layout,
            'is_guest_checkout_active' => pgs_woo_api_is_guest_checkout(),
            'is_login'                 => $is_login,
            'is_slider'                => $is_slider,
			'is_rtl'                   => $is_rtl,
			'site_language'            => get_bloginfo('language'),
			'is_terawallet_active'     => class_exists( 'WooWallet' ),
        );

		$responce['is_user_exists'] = 'not_provided';
		if ( version_compare( $this->app_ver, '4.4.0', '>=' ) && ! empty( $this->user_id ) ) {
			$is_user_exists = $this->is_user_exists( $this->user_id, $this->params, $this->wp_rest_request );

			$responce['is_user_exists'] = $is_user_exists;
		}

        return $responce;
    }

    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/home
    * app-ver : ####
    */
    public function pgs_woo_api_app_home( WP_REST_Request $request ) {

        $this->wp_rest_request = $request;
		$this->params          = $this->wp_rest_request->get_params();

		if ( isset( $this->params['user_id'] ) && ! empty( $this->params['user_id'] ) ) {
			$this->user_id = absint( $this->params['user_id'] );
		}

		if ( isset( $this->params['app-ver'] ) && ! empty( $this->params['app-ver'] ) ) {
    		$this->app_ver = $this->params['app-ver'];
    	}

		$input   = file_get_contents( "php://input" );
        $request = json_decode( $input, true );

		pgs_woo_api_set_currency();

        /**
         * Check currency switcher plugin is active
         */
        $this->is_currency_switcher_active = pgs_woo_api_is_currency_switcher_active();

        /**
         * Check yith featured video plugin is active
         */
        $this->is_yith_featured_video_active = pgs_woo_api_is_yith_featured_video_active();//cehck plugin active


        $lang='';
        $is_wpml_active = pgs_woo_api_is_wpml_active();
        if($is_wpml_active){
            $lang = pgs_woo_api_wpml_get_lang();
            if(!empty($lang)){
                $lang_prifix = '_'.$lang;
                $pgs_woo_api_wpml_home_option = get_option('pgs_woo_api_home_option'.$lang_prifix);
            }
        }

		$pgs_woo_api_home_option = get_option('pgs_woo_api_home_option', array() );

        $price_formate_option = get_woo_price_formate_option_array();

        $carousel_data = array();

		if(!empty($lang)){
			$pgs_woo_api_home_option['app_logo']            = $this->pgs_woo_api_get_app_logo($pgs_woo_api_wpml_home_option,$pgs_woo_api_home_option);
			$pgs_woo_api_home_option['app_logo_light']      = $this->pgs_woo_api_get_app_logo_light($pgs_woo_api_wpml_home_option,$pgs_woo_api_home_option);
			$pgs_woo_api_home_option['main_category']       = $this->pgs_woo_api_get_main_category($pgs_woo_api_home_option,$lang);
			$pgs_woo_api_home_option['main_slider']         = $this->pgs_woo_api_get_main_slider($pgs_woo_api_wpml_home_option);
			$pgs_woo_api_home_option['category_banners']    = $this->pgs_woo_api_get_category_banners($pgs_woo_api_wpml_home_option);
			$pgs_woo_api_home_option['banner_ad']           = $this->pgs_woo_api_get_banner_ads($pgs_woo_api_wpml_home_option);
			$feature_box_data                               = $this->pgs_woo_api_get_feature_box($pgs_woo_api_wpml_home_option);
			$pgs_woo_api_home_option['feature_box_heading'] = $feature_box_data['feature_box_heading'];
			$pgs_woo_api_home_option['feature_box_status']  = $feature_box_data['feature_box_status'];
			$pgs_woo_api_home_option['feature_box']         = $feature_box_data['feature_box'];
			$carousel_data                                  =  $this->pgs_woo_api_get_home_products_carousel_data($pgs_woo_api_wpml_home_option,$this->app_ver);
        } else {
			$pgs_woo_api_home_option['app_logo']            = $this->pgs_woo_api_get_app_logo($pgs_woo_api_home_option);
			$pgs_woo_api_home_option['app_logo_light']      = $this->pgs_woo_api_get_app_logo_light($pgs_woo_api_home_option);
			$pgs_woo_api_home_option['main_category']       = $this->pgs_woo_api_get_main_category($pgs_woo_api_home_option);
			$pgs_woo_api_home_option['main_slider']         = $this->pgs_woo_api_get_main_slider($pgs_woo_api_home_option);
			$pgs_woo_api_home_option['category_banners']    = $this->pgs_woo_api_get_category_banners($pgs_woo_api_home_option);
			$pgs_woo_api_home_option['banner_ad']           = $this->pgs_woo_api_get_banner_ads($pgs_woo_api_home_option);
			$feature_box_data                               = $this->pgs_woo_api_get_feature_box($pgs_woo_api_home_option);
			$pgs_woo_api_home_option['feature_box_heading'] = $feature_box_data['feature_box_heading'];
			$pgs_woo_api_home_option['feature_box_status']  = $feature_box_data['feature_box_status'];
			$pgs_woo_api_home_option['feature_box']         = $feature_box_data['feature_box'];
			$carousel_data                                  =  $this->pgs_woo_api_get_home_products_carousel_data($pgs_woo_api_home_option,$this->app_ver);
        }

        if($this->app_ver !== ''){
            $products_view_orders = array();
            if(isset($carousel_data['products_carousel'])){
                foreach($carousel_data['products_carousel'] as $key => $val){
                    $products_view_orders[]=array(
                        "name"=>$key
                    );
                }
            }
            $pgs_woo_api_home_option['products_view_orders'] = $products_view_orders;
            $pgs_woo_api_home_option['products_carousel'] = ( isset( $carousel_data['products_carousel'] ) ) ? $carousel_data['products_carousel'] : array();
        } else {
            unset($pgs_woo_api_home_option['products_carousel']);
            $pgs_woo_api_home_option['popular_products'] = $carousel_data['popular_products'];
            $pgs_woo_api_home_option['scheduled_sale_products'] = $carousel_data['scheduled_sale_products'];
        }
        $pgs_woo_api_home_option['static_page'] = $this->pgs_woo_api_get_static_pages($pgs_woo_api_home_option,$lang);
        $pgs_woo_api_home_option['info_pages'] = $this->pgs_woo_api_get_info_pages($pgs_woo_api_home_option,$lang);
        $pgs_woo_api_home_option['all_categories'] = $this->pgs_woo_api_cat_list();
        $pgs_woo_api_home_option['is_wishlist_active'] = pgs_woo_api_is_wishlist_active();
        $pgs_woo_api_home_option['is_currency_switcher_active'] = $this->is_currency_switcher_active;
        $pgs_woo_api_home_option['is_yith_featured_video_active'] = $this->is_yith_featured_video_active;
        $pgs_woo_api_home_option['is_order_tracking_active'] = pgs_woo_api_is_order_tracking_active();
        $pgs_woo_api_home_option['is_reward_points_active'] = pgs_woo_api_is_reward_points_active();
        $pgs_woo_api_home_option['is_guest_checkout_active'] = pgs_woo_api_is_guest_checkout();
        $is_wpml_active = pgs_woo_api_is_wpml_active();
        if($is_wpml_active){
            $pgs_api_is_wpml_status = (isset($pgs_woo_api_home_option['pgs_api_is_wpml']))?$pgs_woo_api_home_option['pgs_api_is_wpml']:'enable';
            if($pgs_api_is_wpml_status == "enable"){
                $is_wpml_active = true;
            } else {
                $is_wpml_active = false;
            }
        }

        $pgs_woo_api_home_option['is_wpml_active'] = $is_wpml_active;
        $pgs_woo_api_home_option['price_formate_options'] = $price_formate_option;

        $pgsiosappurl = get_option('pgs_ios_app_url');
        $pgs_ios_app_url = (isset($pgsiosappurl))?$pgsiosappurl:'';
        $pgs_woo_api_home_option['ios_app_url'] = $pgs_ios_app_url;
        $site_language = get_bloginfo('language');
        $pgs_woo_api_home_option['site_language'] = $site_language;
        $pgs_woo_api_home_option['wpml_languages'] = $this->pgs_woo_api_get_all_wpml_langs($is_wpml_active);
		$pgs_woo_api_home_option['pgs_woo_api_deliver_pincode'] = $this->pgs_woo_api_is_pincode_plugin();
		$pgs_woo_api_home_option['pgs_woo_api_web_view_pages'] = $this->pgs_woo_api_app_get_web_view_page_list();

		$pgs_api_is_custom_section_status = (isset($pgs_woo_api_home_option['product_banners_cat_value']))?$pgs_woo_api_home_option['product_banners_cat_value']:'enable';

		if ( 'enable' === $pgs_api_is_custom_section_status ) {
			$is_custom_section_active = true;
			$product_banners_title    = ( isset( $pgs_woo_api_home_option['product_banners_title'] ) && ! empty( $pgs_woo_api_home_option['product_banners_title'] ) ) ? $pgs_woo_api_home_option['product_banners_title'] : '';
			$custom_section           = ( isset( $pgs_woo_api_home_option['custom_section'] ) && ! empty( $pgs_woo_api_home_option['custom_section'] ) ) ? $pgs_woo_api_home_option['custom_section'] : array();
		} else {
			$is_custom_section_active = false;
			$product_banners_title    = '';
			$custom_section           = array();
		}

		$pgs_woo_api_home_option['custom_section'] = $this->pgs_woo_api_get_all_custom_section( $is_custom_section_active, $product_banners_title, $custom_section, $app_ver = '' );

        $checkout_redirect_urls  = $this->pgs_woo_api_get_checkout_redirect_url();
        $pgs_woo_api_home_option['checkout_redirect_url'] = $checkout_redirect_urls;
        $pgs_woo_api_home_option['pgs_app_contact_info'] = $this->pgs_woo_api_get_contact_info($pgs_woo_api_home_option);

        /**
         *  Get App Assets app color
         */
        $app_color = array(
            'header_color' => '',
            'primary_color' => '#60A727',
            'secondary_color' => ''
        );
        $app_assets = get_option('pgs_woo_api_app_assets_options');
        if(isset($app_assets) && !empty($app_assets)){
            if(isset($app_assets['app_assets']['app_color']) && !empty($app_assets['app_assets']['app_color'])){
                $app_color = $app_assets['app_assets']['app_color'];
                if ( version_compare( $this->app_ver, '4.0', '>=' ) ) {
                    $app_color['header_color'] = $app_assets['app_assets']['app_color']['primary_color'];
                } else {
                    $header_color = $app_assets['app_assets']['app_color']['primary_color'];
                    if ( isset( $app_assets['app_assets']['app_color']['header_color'] ) && ! empty( $app_assets['app_assets']['app_color']['header_color'] ) ) {
                        $header_color = $app_assets['app_assets']['app_color']['header_color'];
                    }
                    $app_color['header_color'] = $header_color;
                }
            }
        }
        $pgs_woo_api_home_option['app_color'] = $app_color;
        $pgs_woo_api_home_option['wc_tax_enabled'] = false;//wc_tax_enabled
        if(wc_tax_enabled()){
            $pgs_woo_api_home_option['wc_tax_enabled'] = true;
            /*$pgs_woo_api_home_option['woocommerce_tax_display_shop'] = "incl";//Including tax
            if("excl" == get_option( 'woocommerce_tax_display_shop' )){
                $pgs_woo_api_home_option['woocommerce_tax_display_shop'] = "excl";//Excluding tax
            }
            $pgs_woo_api_home_option['woocommerce_tax_display_cart'] = "incl";//Including tax
            if("excl" == get_option( 'woocommerce_tax_display_cart' )){
                $pgs_woo_api_home_option['woocommerce_tax_display_cart'] = "excl";//Excluding tax
            }*/
        }
        $pgs_woo_api_home_option['woocommerce_tax_display_shop'] = get_option( 'woocommerce_tax_display_shop' );
        $pgs_woo_api_home_option['woocommerce_tax_display_cart'] = get_option( 'woocommerce_tax_display_cart' );
        $pgs_woo_api_home_option['woocommerce_price_display_suffix'] = get_option( 'woocommerce_price_display_suffix' );
        $pgs_woo_api_home_option['woocommerce_tax_total_display'] = get_option( 'woocommerce_tax_total_display' );

        $is_rtl = false;
        if ( is_rtl() ) {
            $is_rtl = true;
        }
        $pgs_woo_api_home_option['is_rtl'] = $is_rtl;
        if($this->is_currency_switcher_active){
            $currency_data = get_option('woocs');
            if(isset($currency_data) && !empty($currency_data)){
				global $WOOCS;
				if( $WOOCS ) {
					$currencies = $WOOCS->get_currencies();
					if(isset($currencies) && !empty($currencies)){
						$pgs_woo_api_home_option['currency_switcher'] = $currencies;
					}
				}
            }
        }
        $data = false;
        if(get_option('app_validation')){
            $data = true;
        }
        $pgs_woo_api_home_option['is_app_validation'] = $data;

        //review varification
		$pgs_woo_api_home_option['woocommerce_review_rating_verification_required'] =get_option( 'woocommerce_review_rating_verification_required' );
		$pgs_woo_api_home_option['woocommerce_enable_reviews'] =get_option( 'woocommerce_enable_reviews' );

		// Set is_terawallet_active.
		$pgs_woo_api_home_option['is_terawallet_active'] = class_exists( 'WooWallet' );

		if ( version_compare( $this->app_ver, '4.0', '>=' ) ) {
			$is_verified = $this->get_verificaion_status( $request );
			$pgs_woo_api_home_option['is_verified'] = $is_verified;
		}

		$pgs_woo_api_home_option['delete_account_alert_title']   = pgs_woo_api_delete_account_alert_title();
		$pgs_woo_api_home_option['delete_account_alert_message'] = pgs_woo_api_delete_account_alert_message();

		$pgs_woo_api_home_option['is_user_exists'] = 'not_provided';
		if ( version_compare( $this->app_ver, '4.4.0', '>=' ) && ! empty( $this->user_id ) ) {
			$is_user_exists = $this->is_user_exists( $this->user_id, $this->params, $this->wp_rest_request );

			$pgs_woo_api_home_option['is_user_exists'] = $is_user_exists;
		}

		return $pgs_woo_api_home_option;
    }


    public function pgs_woo_api_get_app_logo($pgs_woo_api_option,$default_lang_app_logo=array()){
        $app_logo_id = (isset($pgs_woo_api_option['app_logo']))?$pgs_woo_api_option['app_logo']:'';
        $app_logo_url = '';
        if(!empty($app_logo_id)){
            $src = wp_get_attachment_image_src($app_logo_id, apply_filters( 'pgs_woo_api_app_logo_image', 'full' ) );
            if(!empty($src)){
                $app_logo_url = $src[0];
            }
    	} else {
            $app_logo_id = (isset($default_lang_app_logo['app_logo_light']))?$default_lang_app_logo['app_logo_light']:'';
            if(!empty($app_logo_id)){
                $src = wp_get_attachment_image_src($app_logo_id, apply_filters( 'pgs_woo_api_app_logo_light_image', 'full' ) );
                if(!empty($src)){
                    $app_logo_url = $src[0];
                }
            }
        }
        return $app_logo_url;
    }

    public function pgs_woo_api_get_app_logo_light($pgs_woo_api_option,$default_lang_logo_light=array()){
        $app_logo_light_id = (isset($pgs_woo_api_option['app_logo_light']))?$pgs_woo_api_option['app_logo_light']:'';
        $app_logo_light_url = '';
        if(!empty($app_logo_light_id)){
            $src = wp_get_attachment_image_src($app_logo_light_id, apply_filters( 'pgs_woo_api_app_logo_light_image', 'full' ) );
            if(!empty($src)){
                $app_logo_light_url = $src[0];
            }
        } else {
            $app_logo_light_id = (isset($default_lang_logo_light['app_logo_light']))?$default_lang_logo_light['app_logo_light']:'';
            if(!empty($app_logo_light_id)){
                $src = wp_get_attachment_image_src($app_logo_light_id, apply_filters( 'pgs_woo_api_app_logo_light_image', 'full' ) );
                if(!empty($src)){
                    $app_logo_light_url = $src[0];
                }
            }
        }
        return $app_logo_light_url;
    }


    public function pgs_woo_api_get_main_category($pgs_woo_api_option,$lang=''){
        $main_category_arr = array();
        if(isset($pgs_woo_api_option['main_category']) && !empty($pgs_woo_api_option['main_category'])){
            $p = 0;
            foreach($pgs_woo_api_option['main_category'] as $key => $val){

                if(isset($val['main_cat_id']) && !empty($val['main_cat_id']) ){
                    $cat_data = get_term_by( 'id',$val['main_cat_id'],'product_cat' );
                    if(!empty($lang)){
                        if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) ) {
                            $original_id = icl_object_id( $val['main_cat_id'], 'product_cat', true, $lang );
                            $val['main_cat_id'] = $original_id;
							$cat_data = get_term_by( 'id', $original_id, 'product_cat' );
                        }
                    }
                    $main_category_arr[$p]['main_cat_id'] = $val['main_cat_id'];
                    $main_category_arr[$p]['main_cat_name'] = html_entity_decode($cat_data->name);

                    $attch_id = get_term_meta( $val['main_cat_id'], 'product_app_cat_thumbnail_id', true );
                    $vsrc = wp_get_attachment_image_src($attch_id, apply_filters( 'pgs_woo_api_main_category_image', 'thumbnail' ) );
                    if(!empty($vsrc)){
                        $main_category_arr[$p]['main_cat_image'] = $vsrc[0];
                    } else {
                        $main_category_arr[$p]['main_cat_image'] = '';
                    }
                    $p++;
                }
            }
        }
        return $main_category_arr;
    }

    public function pgs_woo_api_get_static_pages($pgs_woo_api_option,$lang=''){
        $static_pagey_arr = array(
            "about_us"=> "",
            "terms_of_use"=> "",
            "privacy_policy"=> ""
        );
        foreach($pgs_woo_api_option['static_page'] as $key => $static_page_id){
            if(isset($static_page_id) && !empty($static_page_id) ){
                if(!empty($lang)){
                    if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) ) {
                        $static_page_id = icl_object_id( $static_page_id, 'post', true, $lang );
                    }
                }
            }
            $static_pagey_arr[$key] = $static_page_id;
        }
        return $static_pagey_arr;
    }

    public function pgs_woo_api_get_info_pages($pgs_woo_api_option,$lang=''){
        $info_pages_arr[] = array( "info_pages_page_id"=> "" );
        foreach($pgs_woo_api_option['info_pages'] as $key => $info_page){
            if(isset($info_page['info_pages_page_id']) && !empty($info_page['info_pages_page_id']) ){
                if(!empty($lang)){
                    if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) ) {
                        $info_page['info_pages_page_id'] = icl_object_id( $info_page['info_pages_page_id'], 'post', true, $lang );
                    }
                }
            }
            $info_pages_arr[$key]['info_pages_page_id'] = $info_page['info_pages_page_id'];
        }
        return $info_pages_arr;
    }

    public function pgs_woo_api_get_main_slider($pgs_woo_api_option){
        $main_slider_arr = array();
        if(isset($pgs_woo_api_option['main_slider']) && !empty($pgs_woo_api_option['main_slider'])){
            $t = 0;
            foreach($pgs_woo_api_option['main_slider'] as $k => $v){
                if(isset($v['upload_image_id']) && !empty($v['upload_image_id']) ){

                    $main_slider_arr[$t]['upload_image_id'] = $v['upload_image_id'];
                    $main_slider_arr[$t]['slider_cat_id'] = $v['slider_cat_id'];
                    $vsrc = wp_get_attachment_image_src($v['upload_image_id'], apply_filters( 'pgs_woo_api_slider_image', 'large' ));
                    if(!empty($vsrc)){
                        $main_slider_arr[$t]['upload_image_url'] = esc_url($vsrc[0]);
                    } else {
                        $main_slider_arr[$t]['upload_image_url'] = '';
                    }
                    $t++;
                }

            }
        }
        return $main_slider_arr;
    }

    public function pgs_woo_api_get_category_banners($pgs_woo_api_option){
        $category_banners_arr = array();
        if(isset($pgs_woo_api_option['category_banners']) && !empty($pgs_woo_api_option['category_banners'])){
            $p = 0;
            foreach($pgs_woo_api_option['category_banners'] as $k => $v){
                if( !empty($v['cat_banners_image_id']) || !empty($v['cat_banners_title']) || !empty($v['cat_banners_cat_id']) ){
                    if( !empty($v['cat_banners_image_id']) ){
                        $category_banners_arr[$p]['cat_banners_image_id'] = $v['cat_banners_image_id'];
                        $vsrc = wp_get_attachment_image_src($v['cat_banners_image_id'], apply_filters( 'pgs_woo_api_cat_banners_image', 'app_thumbnail' ));
                        if(!empty($vsrc)){
                            $category_banners_arr[$p]['cat_banners_image_url'] = esc_url($vsrc[0]);
                        } else {
                            $category_banners_arr[$p]['cat_banners_image_url'] = '';
                        }
                    }
                    $category_banners_arr[$p]['cat_banners_cat_id'] = (isset($v['cat_banners_cat_id']))?$v['cat_banners_cat_id']:'';
                    if( !empty($v['cat_banners_title']) ){
                        $category_banners_arr[$p]['cat_banners_title'] = stripslashes($v['cat_banners_title']);
                    } else {
                        $category_banners_arr[$p]['cat_banners_title'] = '';
                    }
                }
                $p++;
            }
        }
        return $category_banners_arr;
    }

    public function pgs_woo_api_get_banner_ads($pgs_woo_api_option){

        $banner_ad_arr = array();
        if(isset($pgs_woo_api_option['banner_ad']) && !empty($pgs_woo_api_option['banner_ad'])){
            $b = 0;
            foreach($pgs_woo_api_option['banner_ad'] as $k => $v){
                if( ( isset($v['banner_ad_image_id']) && !empty($v['banner_ad_image_id']) ) && ( isset($v['banner_ad_cat_id']) && !empty($v['banner_ad_cat_id']) ) ) {
                    $banner_ad_image_id = $v['banner_ad_image_id'];
                    $vsrc = wp_get_attachment_image_src($banner_ad_image_id, apply_filters( 'pgs_woo_api_banner_ad_image', 'large' ) );
                    if(!empty($vsrc)){
                        $banner_ad_arr[$b]['banner_ad_image_url'] = $vsrc[0];
                    } else {
                        $banner_ad_arr[$b]['banner_ad_image_url'] = '';
                    }
                    $banner_ad_arr[$b]['banner_ad_image_id'] = $banner_ad_image_id;

                    $banner_ad_arr[$b]['banner_ad_cat_id'] = $v['banner_ad_cat_id'];
                    $b++;
                }
            }
        }
        return $banner_ad_arr;
    }


    public function pgs_woo_api_get_feature_box($pgs_woo_api_option){
        if(isset($pgs_woo_api_option['feature_box_heading'])){
            $pgs_woo_api_home_option['feature_box_heading'] = stripslashes($pgs_woo_api_option['feature_box_heading']);
        } else {
            $pgs_woo_api_home_option['feature_box_heading'] = '';
        }

        $feature_box_status = (isset($pgs_woo_api_option['feature_box_status']) && !empty($pgs_woo_api_option['feature_box_status']))?$pgs_woo_api_option['feature_box_status']:'enable';
        $pgs_woo_api_home_option['feature_box_status'] = $feature_box_status;
        if($feature_box_status == "enable"){
            $f = 0;
            if(isset($pgs_woo_api_option['feature_box'])&& !empty($pgs_woo_api_option['feature_box'])){
                foreach($pgs_woo_api_option['feature_box'] as $key => $val){
                    $pgs_woo_api_home_option['feature_box'][$f]['feature_title'] = (isset($val['feature_title']))?$val['feature_title']:'';
                    $pgs_woo_api_home_option['feature_box'][$f]['feature_content'] = (isset($val['feature_content']))?$val['feature_content']:'';
                    if(isset($val['feature_image_id']) && !empty($val['feature_image_id']) ){
                        $attch_id = $val['feature_image_id'];
                        $vsrc = wp_get_attachment_image_src($attch_id, apply_filters( 'pgs_woo_api_feature_image', 'thumbnail' ) );
                        if(!empty($vsrc)){
                            $pgs_woo_api_home_option['feature_box'][$f]['feature_image'] = $vsrc[0];
                        } else {
                            $pgs_woo_api_home_option['feature_box'][$f]['feature_image'] = '';
                        }
                    }
                    $f++;
                }
            } else {
                $pgs_woo_api_home_option['feature_box'] = array();
            }
        } else {
            $pgs_woo_api_home_option['feature_box'] = array();
        }
        return $pgs_woo_api_home_option;
    }


    public function pgs_woo_api_get_home_products_carousel_data($pgs_woo_api_option,$app_ver=''){
		$i                       = 0;
		$orderby                 = 'date';
		$order                   = 'desc';
		$no_of_items             = 4;
		$pgs_woo_api_home_option = array();

        if($app_ver !== ''){
            if(isset($pgs_woo_api_option['products_carousel'])){
                foreach($pgs_woo_api_option['products_carousel'] as $k => $v){
					if( $k == 'feature_products' ){
						$pgs_woo_api_home_option['products_carousel'][$k]['status']       = $v['status'];
						$pgs_woo_api_home_option['products_carousel'][$k]['title']        = $v['title'];
						$pgs_woo_api_home_option['products_carousel'][$k]['screen_order'] = $i;
						$pgs_woo_api_home_option['products_carousel'][$k]['products']     = $this->pgs_woo_api_get_feature_products_list( $no_of_items, $show='featured', $orderby, $order );
					} elseif( $k == 'recent_products' ){
						$pgs_woo_api_home_option['products_carousel'][$k]['status']       = $v['status'];
						$pgs_woo_api_home_option['products_carousel'][$k]['title']        = $v['title'];
						$pgs_woo_api_home_option['products_carousel'][$k]['screen_order'] = $i;
						$pgs_woo_api_home_option['products_carousel'][$k]['products']     = $this->pgs_woo_api_get_recent_products_list( $no_of_items, $show = 'recent', $orderby, $order );
					} elseif( $k == 'special_deal_products' ){
						$pgs_woo_api_home_option['products_carousel'][$k]['status']       = $v['status'];
						$pgs_woo_api_home_option['products_carousel'][$k]['title']        = $v['title'];
						$pgs_woo_api_home_option['products_carousel'][$k]['screen_order'] = $i;
						$pgs_woo_api_home_option['products_carousel'][$k]['products']     = $this->pgs_woo_api_scheduled_sale_products( $no_of_items, $app_ver );
					} elseif( $k == 'popular_products' ){
						$pgs_woo_api_home_option['products_carousel'][$k]['status']       = $v['status'];
						$pgs_woo_api_home_option['products_carousel'][$k]['title']        = $v['title'];
						$pgs_woo_api_home_option['products_carousel'][$k]['screen_order'] = $i;
						$pgs_woo_api_home_option['products_carousel'][$k]['products']     = $this->pgs_woo_api_get_popular_products( $no_of_items, $show = 'popular', $orderby, $order, $app_ver );
					} elseif( $k == 'top_rated_products' ){
						$pgs_woo_api_home_option['products_carousel'][$k]['status']       = $v['status'];
						$pgs_woo_api_home_option['products_carousel'][$k]['title']        = $v['title'];
						$pgs_woo_api_home_option['products_carousel'][$k]['screen_order'] = $i;
						$pgs_woo_api_home_option['products_carousel'][$k]['products']     = $this->pgs_woo_api_get_top_rated_products( $no_of_items, $show = 'top_rated', $orderby, $order, $app_ver );
                    }
                    $i++;
                }
            }
        } else {
            unset($pgs_woo_api_option['products_carousel']);
            $pgs_woo_api_home_option['popular_products']        = $this->pgs_woo_api_get_popular_products($no_of_items,$show='popular',$orderby,$order,$app_ver);
            $pgs_woo_api_home_option['scheduled_sale_products'] = $this->pgs_woo_api_scheduled_sale_products($no_of_items,$app_ver);
        }
        return $pgs_woo_api_home_option;
    }

    public function pgs_woo_api_get_all_wpml_langs($is_wpml_active){
        $lang_data = array();$pgs_woo_api_icl_get_languages=array();
        if($is_wpml_active){
            global $wpdb,$sitepress;
            $ls_settings = get_option('icl_sitepress_settings');
            $icl_get_languages = icl_get_languages();
            if(!empty($icl_get_languages)){
                foreach($icl_get_languages as $key => $lan){
                    $site_language = (isset($lan['default_locale']))?str_replace( '_', '-', $lan['default_locale'] ):'';
                    if(isset($ls_settings['icl_lso_flags']) && $ls_settings['icl_lso_flags']==1){
						 $disp_language = icl_disp_language($lan['native_name'], $lan['translated_name']);
					}else{
						 $disp_language = icl_disp_language($lan['native_name']);
					}
                    $pgs_woo_api_icl_get_languages[] = array(
                        "code" => $icl_get_languages[$key]['code'],
                        "id" => $icl_get_languages[$key]['id'],
                        "native_name" => $icl_get_languages[$key]['native_name'],
                        //"major" => $icl_get_languages[$key]['major'],
                        "active" => $icl_get_languages[$key]['active'],
                        "default_locale" => $icl_get_languages[$key]['default_locale'],
                        //"encode_url" => $icl_get_languages[$key]['encode_url'],
                        //"tag" => $icl_get_languages[$key]['tag'],
                        "translated_name" => $icl_get_languages[$key]['translated_name'],
                        //"url" => $icl_get_languages[$key]['url'],
                        "language_code" => $icl_get_languages[$key]['language_code'],
                        "disp_language" => $disp_language,
                        "site_language" => $site_language,
                        "is_rtl" => $sitepress->is_rtl( $key )
                    );
                }
            }
            $lang_data = $pgs_woo_api_icl_get_languages;
        }
        return $lang_data;
    }

    public function pgs_woo_api_cat_list(){
        $taxonomy     = 'product_cat';
        $orderby      = 'name';
        $show_count   = 1;      // 1 for yes, 0 for no
        $pad_counts   = 1;      // 1 for yes, 0 for no
        $hierarchical = 1;      // 1 for yes, 0 for no
        $title        = '';
        $empty        = 0;

        $args = array(
             'taxonomy'     => $taxonomy,
             'orderby'      => $orderby,
             'show_count'   => $show_count,
             'pad_counts'   => $pad_counts,
             'hierarchical' => $hierarchical,
             'title_li'     => $title,
             'hide_empty'   => $empty
        );
        $all_categories = get_categories( $args );

        $data = array();
        if(isset($all_categories) && !empty($all_categories)){
            foreach ($all_categories as $cat) {

                $product_app_cat_thumbnail_id = get_term_meta($cat->term_id, 'product_app_cat_thumbnail_id', true);
                $vsrc = wp_get_attachment_image_src($product_app_cat_thumbnail_id, apply_filters( 'pgs_woo_api_app_cat_thumbnail_image', 'thumbnail' ) );
                if(!empty($vsrc)){
                    $main_cat_id_image = $vsrc[0];
                } else {
                    $main_cat_id_image = '';
                }

                $data[] = array(
                    'description' => $cat->category_description,
                    'id' => $cat->term_id,
                    'image' => array(
                        'src' => $main_cat_id_image,
                    ),
                    'name' => html_entity_decode($cat->name),
                    'parent' => $cat->category_parent,
                    'slug' => $cat->slug,
                );
            }
        }
        return $data;
    }

	public function pgs_woo_api_get_all_custom_section($is_custom_section_active,$product_banners_title,$custom_section,$app_ver){
       $custom_section_status = $is_custom_section_active;
	   if($custom_section_status == "true")
	   {
		    $product_detail=array();
		    if(isset($custom_section) && !empty($custom_section))
			{
				foreach ($custom_section as $product) {
					if(!empty($product['product_banners_cat_id'])){

						// Get translated product ID.
						$translated_product_id = pgs_woo_api_lang_object_ids( $product['product_banners_cat_id'], 'post' );

						// Get $product object from product ID
						$product_data = wc_get_product( $translated_product_id );

						if ( ! is_a( $product_data, 'WC_Product' ) ) {
							continue;
						}

						if ( $product_data && 'hidden' !== $product_data->get_catalog_visibility() ) {
							$product_detail[] = $this->pgs_woo_api_set_productdata( $product_data, $this->is_currency_switcher_active, $app_ver );
						}
					}
				}
			}
		   $data=$product_detail;
	   }
	   else
	   {
		   $data=array();
	   }


       return $data;
    }

    public function pgs_woo_api_get_recent_products_list($no_of_items,$show,$orderby,$order,$app_ver=''){
        $query = $this->pgs_woo_api_get_product_crousel_query($no_of_items,$show,$orderby,$order);
        $result = $this->pgs_woo_api_get_product_crousel_data($query,$app_ver);
        return $result;
    }

    public function pgs_woo_api_get_feature_products_list($no_of_items,$show,$orderby,$order,$app_ver=''){
        $query = $this->pgs_woo_api_get_product_crousel_query($no_of_items,$show,$orderby,$order);
        $result = $this->pgs_woo_api_get_product_crousel_data($query,$app_ver);
        return $result;
    }

    public function pgs_woo_api_get_top_rated_products($no_of_items,$show='top_rated',$orderby,$order,$app_ver){
        $query_args = array(
			'posts_per_page' => $no_of_items,
			'no_found_rows'  => 1,
			'post_status'    => 'publish',
			'post_type'      => 'product',
            'meta_key'       => '_wc_average_rating',
            'meta_query' => array(
                array(
                    'key'     => '_wc_average_rating',
                    'value'   => 0,
                    'compare' => '>'
                )
            ),
			'orderby'        => array(
                'meta_value_num' => 'DESC',
                'ID' => 'ASC',
            ),
			'order'          => $order,
			'tax_query'      => array()
		);

        $product_visibility_terms  = wc_get_product_visibility_term_ids();
        $productdata = array();
		// Hide out of stock products.
		if ( 'yes' === get_option( 'woocommerce_hide_out_of_stock_items' ) ) {
			$product_visibility_not_in[] = $product_visibility_terms['outofstock'];
		}

        if ( !empty( $query_args['tax_query'] ) ) {
			$query_args['tax_query']['relation'] = 'AND';
		}

        if ( ! empty( $product_visibility_not_in ) ) {
			$query_args['tax_query'][] = array(
				'taxonomy' => 'product_visibility',
				'field'    => 'term_taxonomy_id',
				'terms'    => $product_visibility_not_in,
				'operator' => 'NOT IN',
			);
		}
		$query_args['tax_query'][] = array(
			'taxonomy' => 'product_visibility',
			'field'    => 'term_taxonomy_id',
			'terms'    => $product_visibility_terms['exclude-from-catalog'],
			'operator' => 'NOT IN',
		);
        $query = new WP_Query( $query_args );
        if ( $query->have_posts() ) {
			while ( $query->have_posts() ) {
				$query->the_post();
                $product = wc_get_product( $query->post->ID );

				if ( ! is_a( $product, 'WC_Product' ) ) {
					continue;
				}

                $productdata[] = $this->pgs_woo_api_set_productdata($product,$this->is_currency_switcher_active,$app_ver);
			}
            return $productdata;
        } else {
            if($app_ver == ''){
                return $error = array();
            } else {
                return $productdata;
            }
        }
		wp_reset_postdata();
    }

    /**
     * Get All Popular Products
     */
    public function pgs_woo_api_get_popular_products($no_of_items,$show,$orderby,$order,$app_ver=''){

        $productdata = array();
        $product_visibility_term_ids = wc_get_product_visibility_term_ids();
        $query_args = array(
			'posts_per_page' => 4,
			'post_status'    => 'publish',
			'post_type'      => 'product',
			'no_found_rows'  => 1,
			'order'          => 'desc',
			'meta_query'     => array(),
			'tax_query'      => array(
				'relation' => 'AND',
			),
		); // WPCS: slow query ok.


		$query_args['tax_query'][] = array(
			'taxonomy' => 'product_visibility',
			'field'    => 'term_taxonomy_id',
			'terms'    => is_search() ? $product_visibility_term_ids['exclude-from-search'] : $product_visibility_term_ids['exclude-from-catalog'],
			'operator' => 'NOT IN',
		);
		$query_args['post_parent'] = 0;
        $query_args['meta_query'] = array(
    		array(
    			'key'     => 'total_sales',
    			'value'   => 0,
                'type'    => 'numeric',
    			'compare' => '>',
    		)
    	);
		$query_args['orderby']  = 'meta_value_num';
        $popular_products = new WP_Query( $query_args );
        if ( $popular_products && $popular_products->have_posts() ) {
            while ( $popular_products->have_posts() ) {
				$popular_products->the_post();
				$product_id = $popular_products->post->ID;

                pgs_woo_api_hook_remove_tax_in_price_html();//Remove include tax in price html
                $product = wc_get_product( $product_id );

				if ( ! is_a( $product, 'WC_Product' ) ) {
					continue;
				}

                $productdata[] = $this->pgs_woo_api_set_productdata($product,$this->is_currency_switcher_active,$app_ver);
			}
            wp_reset_postdata();
            return $productdata;
        } else {
            if($app_ver == ''){
                return $error = array();
            } else {
                return $productdata;
            }
        }
    }

    /**
     * Gey All Scheduled Sale Products OR special_deal_products
     */
    public function pgs_woo_api_scheduled_sale_products($no_of_items=10,$app_ver=''){
		$productdata = array();

        $query_args = array(
			'posts_per_page' => -1,
			'post_status'    => 'publish',
			'post_type'      => array(
				'product',
				'product_variation',
			),
			'order'          => 'ASC',
			'meta_query'     => array(),
        ); // WPCS: slow query ok.

        $query_args['meta_query'] = array(
    		array(
    			'key'     => '_sale_price_dates_to',
    			'value'   => time(),
                'type'    => 'numeric',
    			'compare' => '>=',
    		)
    	);
		$query_args['orderby']   = 'post_title';
        $scheduled_sale_products = new WP_Query( $query_args );
		$product_ids_on_sale     = array();

        if ( $scheduled_sale_products && $scheduled_sale_products->have_posts() ) {
            while ( $scheduled_sale_products->have_posts() ) {
				$scheduled_sale_products->the_post();

				$product_id = $scheduled_sale_products->post->ID;
				$product    = wc_get_product( $product_id );

				if ( ! is_a( $product, 'WC_Product' ) ) {
					continue;
				}

				if ( 0 == (int) $product->get_parent_id() ) {
					$product_ids_on_sale[ $product_id ] = $product;
				} else {
					$parent_product = wc_get_product( $product->get_parent_id() );
					if ( ! isset( $product_ids_on_sale[ $product->get_parent_id() ] ) ) {
						$product_ids_on_sale[ $product->get_parent_id() ] = $parent_product;
					}

				}
			}
            wp_reset_postdata();
        }

        $product_ids_on_sale = array_unique($product_ids_on_sale);

        if ( !empty( $product_ids_on_sale ) ) {
                foreach($product_ids_on_sale as $product_id => $product ){
					$from        = get_post_meta($product_id,'_sale_price_dates_from',true);
					$now         = new DateTime();
					$future_date = new DateTime(date('Y-m-d').' 24:00:00');

                    $interval  = $future_date->diff($now);
                    $deal_life = array(
                        'hours'   => $interval->format('%h'),
                        'minutes' => $interval->format('%i'),
                        'seconds' => $interval->format('%s')
                    );
                    if( $from <= time() ) {
                        $data = $this->pgs_woo_api_set_productdata($product,$this->is_currency_switcher_active,$app_ver);
                        $per = $this->pgs_woo_api_get_max_discount_percentage($product,$data);
                        $data['deal_life'] = $deal_life;
                        $data['percentage'] = $per;
                        $productdata[] = $data;
                    }
                }
                if( $app_ver == '' ) {
                    $sts = array(
                        "status"   => "success",
                        "products" => $productdata
                    );
                    return $sts;
                } else {
                    return $productdata;
                }
        } else {
            if($app_ver == ''){
                $error['status'] = "error";
                $error['message'] = esc_html__("No product found","pgs-woo-api");
                return $error;
            } else {
                return $productdata;
            }
        }
    }


    public function pgs_woo_api_get_product_crousel_data($loop,$app_ver=''){
		$pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
        if($loop->have_posts()){
            while ( $loop->have_posts() ) : $loop->the_post();//global $product;
                pgs_woo_api_hook_remove_tax_in_price_html();//Remove include tax in price html

                $is_add_to_cart = (isset($pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']) && !empty($pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']))?$pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']:'enable';
				if($is_add_to_cart=='enable'){
					//if condition for add to cart option goes here
					$product_id = $loop->post->ID;
					$data = $this->get_products_data($product_id);

					$seller_info = $this->pgs_woo_api_get_seller_short_details($product_id);
					$data['seller_info'] = $seller_info;


					//additional information
					if (has_post_thumbnail( $product_id )){
						$image = '';
						$image = get_the_post_thumbnail_url($product_id, apply_filters( 'pgs_woo_api_app_thumbnail_image', 'app_thumbnail' ));
						if(empty($image)){
							$image = woocommerce_placeholder_img_src();
						}
					} else {
						$image = woocommerce_placeholder_img_src();
					}
					$product = wc_get_product( $product_id );
					$data['image']=$image;
					$data['title']=$data['name'];
					$average = $product->get_average_rating();
					$data['rating'] = ($average == '') ? "0" : $average;

					/**
					 * Filters the product data.
					 *
					 * @param array      $data                        The array of product data to be filtered.
					 * @param WC_Product $product                     Product instance.
					 * @param bool       $is_currency_switcher_active Whether currency switcher is active.
					 * @param string     $app_ver                     App version.
					 * @param string     $is_add_to_cart              Whether add to cart enabled or disabled.
					 */
					$data = apply_filters( 'pgs_woo_api_get_productdata', $data, $product, $this->is_currency_switcher_active, $app_ver, $is_add_to_cart );

				}
				else{
					$product = wc_get_product( $loop->post->ID );
					$data = $this->pgs_woo_api_set_productdata($product,$this->is_currency_switcher_active,$app_ver);
					$per = $this->pgs_woo_api_get_max_discount_percentage($product,$data);
					$data['percentage'] = $per;
				}
                $productdata[] = $data;
            endwhile;
        } else {
            $productdata = array();
        }
        wp_reset_query();
        return $productdata;
    }

    public function pgs_woo_api_get_product_crousel_query($number,$show,$orderby,$order){
        $product_visibility_term_ids = wc_get_product_visibility_term_ids();
		$query_args = array(
			'posts_per_page' => $number,
			'post_status'    => 'publish',
			'post_type'      => 'product',
			'no_found_rows'  => 1,
			//'order'          => $order,
			'meta_query'     => array(),
			'tax_query'      => array(
				'relation' => 'AND',
			),
		); // WPCS: slow query ok.

		if ( 'yes' === get_option( 'woocommerce_hide_out_of_stock_items' ) ) {
			$query_args['tax_query'] = array(
				array(
					'taxonomy' => 'product_visibility',
					'field'    => 'term_taxonomy_id',
					'terms'    => $product_visibility_term_ids['outofstock'],
					'operator' => 'NOT IN',
				),
			); // WPCS: slow query ok.
		}

		switch ( $show ) {

			case 'featured':
				$query_args['tax_query'][] = array(
					'taxonomy' => 'product_visibility',
					'field'    => 'term_taxonomy_id',
					'terms'    => $product_visibility_term_ids['featured'],
				);
				$query_args['tax_query'][] = array(
					'taxonomy' => 'product_visibility',
					'field'    => 'term_taxonomy_id',
					'terms'    => $product_visibility_term_ids['exclude-from-catalog'],
					'operator' => 'NOT IN',
				);
				break;
			case 'recent':
				$query_args['tax_query'][] = array(
					'taxonomy' => 'product_visibility',
					'field'    => 'term_taxonomy_id',
					'terms'    => $product_visibility_term_ids['exclude-from-catalog'],
					'operator' => 'NOT IN',
				);
				break;
		}

		switch ( $orderby ) {
			case 'price':
				$query_args['meta_key'] = '_price'; // WPCS: slow query ok.
				$query_args['orderby']  = 'meta_value_num';
				break;
			case 'rand':
				$query_args['orderby'] = 'rand';
				break;
			case 'sales':
				$query_args['meta_key'] = 'total_sales'; // WPCS: slow query ok.
				$query_args['orderby']  = 'meta_value_num';
				break;
			default:
				$query_args['orderby'] = 'date';
		}
		return new WP_Query( apply_filters( 'pgs_woo_api_get_product_crousel_query', $query_args ) );
    }


    public function pgs_woo_api_get_checkout_redirect_url(){
        //checkout redirect url
        $pgs_woo_api_checkout_custom_redirect_urls = get_option('pgs_woo_api_checkout_custom_redirect_urls');
        $redirect_urls=array();
        if(!empty($pgs_woo_api_checkout_custom_redirect_urls)){
            $urls=explode( "\n", $pgs_woo_api_checkout_custom_redirect_urls );
            foreach($urls as $url){
                if(!empty($url)){
                    $url = str_replace("\r","",$url);
                    if(!empty($url)){
                        $redirect_urls[]= $url;
                    }
                }
            }
        }
        return $redirect_urls;
    }

    public function pgs_woo_api_get_contact_info($pgs_woo_api_home_option){
        if(!isset($pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_floating_button'])){
            if(!empty($pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_no'])){
                $pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_floating_button'] = 'enable';
            } else {
                $pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_floating_button'] = 'disable';
            }
        }
        return $pgs_woo_api_home_option['pgs_app_contact_info'];
    }


    public function pgs_woo_api_set_productdata($product,$is_currency_switcher_active,$app_ver){

        $product_id = $product->get_id();
        $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
		$is_add_to_cart = (isset($pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']) && !empty($pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']))?$pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']:'enable';

        if($is_add_to_cart=='enable'){
			$data = $this->get_products_data($product_id);
			//additional information
			if (has_post_thumbnail( $product_id )){
				$image = '';
				$image = get_the_post_thumbnail_url($product_id, apply_filters( 'pgs_woo_api_app_thumbnail_image', 'app_thumbnail' ));
				if(empty($image)){
					$image = woocommerce_placeholder_img_src();
				}
			} else {
				$image = woocommerce_placeholder_img_src();
			}
			$data['image']=$image;
			$data['title']=$data['name'];
			$average = $product->get_average_rating();
			$data['rating'] = ($average == '') ? "0" : $average;

			// Seller Info
			$seller_info = $this->pgs_woo_api_get_seller_short_details($product_id);
			$data['seller_info'] = $seller_info;

			/**
			 * Filters the product data.
			 *
			 * @param array      $data                        The array of product data to be filtered.
			 * @param WC_Product $product                     Product instance.
			 * @param bool       $is_currency_switcher_active Whether currency switcher is active.
			 * @param string     $app_ver                     App version.
			 * @param string     $is_add_to_cart              Whether add to cart enabled or disabled.
			 */
			$data = apply_filters( 'pgs_woo_api_get_productdata', $data, $product, $is_currency_switcher_active, $app_ver, $is_add_to_cart );

			return $data;
		}
		else{

            if (has_post_thumbnail( $product_id )){
                $image = '';
                $image = get_the_post_thumbnail_url($product_id, apply_filters( 'pgs_woo_api_app_thumbnail_image', 'app_thumbnail' ));
                if(empty($image)){
                    $image = woocommerce_placeholder_img_src();
                }
            } else {
            $image = woocommerce_placeholder_img_src();
            }

            $price_html = $product->get_price_html();
            $regular_price = $product->get_regular_price();
            $sale_price = $product->get_sale_price();
            $get_price = $product->get_price();
            $wc_tax_enabled = wc_tax_enabled();
            $tax_status =  'none';
            $tax_class = '';
            if($wc_tax_enabled){
                $tax_price = wc_get_price_to_display( $product );	//tax
                $price_excluding_tax = wc_get_price_excluding_tax( $product );
                $price_including_tax = wc_get_price_including_tax( $product );
                $tax_status =  $product->get_tax_status();
                $tax_class = $product->get_tax_class();
            }
            if($is_currency_switcher_active){
                $regular_price = $this->pgs_woo_api_update_currency_rate($regular_price);
                $sale_price = $this->pgs_woo_api_update_currency_rate($sale_price);
                $get_price = $this->pgs_woo_api_update_currency_rate($get_price);
                if($wc_tax_enabled){
                    $tax_price = $this->pgs_woo_api_update_currency_rate($tax_price);
                    $price_excluding_tax = $this->pgs_woo_api_update_currency_rate($price_excluding_tax);
                    $price_including_tax = $this->pgs_woo_api_update_currency_rate($price_including_tax);
                }
            }
            $tax_price = (isset($tax_price))?$tax_price:'';
            $price_including_tax = (isset($price_including_tax))?$price_including_tax:'';
            $price_excluding_tax = (isset($price_excluding_tax))?$price_excluding_tax:'';
            /*
            $price = array(
                'regular_price' => $regular_price,
                'sale_price' => $sale_price,
                'price' => $get_price,
                'tax_price' => $tax_price,//tax
                'price_including_tax' => $price_including_tax,
                'price_excluding_tax' => $price_excluding_tax,
                'tax_status' =>  $tax_status,
                'tax_class' => $tax_class
            );
            */
            $average = $product->get_average_rating();
            $return = array(
                'id' => $product_id,
                'title' => $product->get_name(),
                'type' => $product->get_type(),
                'on_sale' => $product->is_on_sale(),
                'image' => $image,
                'price_html' => $price_html,
                'regular_price' => $regular_price,
                'sale_price' => $sale_price,
                'price' => $get_price,
                'tax_price' => $tax_price,//tax
                'price_including_tax' => $price_including_tax,
                'price_excluding_tax' => $price_excluding_tax,
                'tax_status' =>  $tax_status,
                'tax_class' => $tax_class,
                'rating' => ($average == '') ? "0" : $average,
                'app_thumbnail'  => $this->get_app_thumbnail($product),
            );

			/**
			 * Filters the product data.
			 *
			 * @param array      $return                      The array of product data to be filtered.
			 * @param WC_Product $product                     Product instance.
			 * @param bool       $is_currency_switcher_active Whether currency switcher is active.
			 * @param string     $app_ver                     App version.
			 * @param string     $is_add_to_cart              Whether add to cart enabled or disabled.
			 */
			$return = apply_filters( 'pgs_woo_api_get_productdata', $return, $product, $is_currency_switcher_active, $app_ver, $is_add_to_cart );

            return $return;
        }
    }


    public function pgs_woo_api_get_max_discount_percentage($product,$data){
        // $regular_price = $data['price']['regular_price'];
        // $sale_price = $data['price']['sale_price'];

		$regular_price = $data['regular_price'];
        $sale_price = $data['sale_price'];

        $per = 0;
        if( $product->is_type( 'simple' ) ){
            if($regular_price > 0 && $sale_price > 0){
                $per = round((($regular_price - $sale_price) / ($regular_price)) * 100);
            }
        } elseif( $product->is_type( 'variable' ) ){

			$available_variations = $product->get_available_variations();

			if($available_variations){

				$percents = array();
				foreach($available_variations as $variations){

					$regular_price = $variations['display_regular_price'];
					$sale_price = $variations['display_price'];

					if ($regular_price){
						$percentage = round( ( ( $regular_price - $sale_price ) / $regular_price ) * 100 );
						$percents[] =  $percentage;
					}
				}

				$max_discount = min($percents);
				$per = $max_discount;
			}
		}
        return $per;
    }

    /**
	 * Get the app thumbnail for single image in list
	 *
	 */
	protected function get_app_thumbnail( $product ) {
		$images = array();$images_url='';
		$attachment_ids = array();

		// Add featured image.
		if ( has_post_thumbnail( $product->get_id() ) ) {
			$attachment_id = $product->get_image_id();
            $images = wp_get_attachment_image_src( $attachment_id, 'app_thumbnail' );
            $images_url = $images[0];
        } else {
            $attachment_ids = $product->get_gallery_image_ids();
    		// Build image data.
    		foreach ( $attachment_ids as $position => $attachment_id ) {
    			$attachment_post = get_post( $attachment_id );
    			if ( is_null( $attachment_post ) ) {
    				continue;
    			}

    			$attachment = wp_get_attachment_image_src( $attachment_id, 'app_thumbnail' );
    			if(!empty($attachment)){
                    $images_url = current( $attachment );
                    break;
    			}
    		}
        }
        if(empty($images_url)){
            $images_url = wc_placeholder_img_src();
        }
		return $images_url;
    }

    /**
	 * Get the attributes for a product or product variation.
	 * @return array
	 */
	public function get_attributes( $product ) {
		$attributes = array();

		if ( $product->is_type( 'variation' ) ) {
			$_product = wc_get_product( $product->get_parent_id() );
			foreach ( $product->get_variation_attributes() as $attribute_name => $attribute ) {
				$name = str_replace( 'attribute_', '', $attribute_name );

				if ( ! $attribute ) {
					continue;
				}

				// Taxonomy-based attributes are prefixed with `pa_`, otherwise simply `attribute_`.
				if ( 0 === strpos( $attribute_name, 'attribute_pa_' ) ) {
					$option_term = get_term_by( 'slug', $attribute, $name );
					$attributes[] = array(
						'id'     => wc_attribute_taxonomy_id_by_name( $name ),
						'name'   => $this->get_attribute_taxonomy_name( $name, $_product ),
						'option' => $option_term && ! is_wp_error( $option_term ) ? $option_term->name : $attribute,
					);
				} else {
					$attributes[] = array(
						'id'     => 0,
						'name'   => $this->get_attribute_taxonomy_name( $name, $_product ),
						'option' => $attribute,
					);
				}
			}
		} else {
			foreach ( $product->get_attributes() as $attribute ) {
				$attributes[] = array(
					'id'         => $attribute['is_taxonomy'] ? wc_attribute_taxonomy_id_by_name( $attribute['name'] ) : 0,
					'name'       => $this->get_attribute_taxonomy_name( $attribute['name'], $product ),
					'position'   => (int) $attribute['position'],
					'visible'    => (bool) $attribute['is_visible'],
					'variation'  => (bool) $attribute['is_variation'],
					'options'    => $this->get_attribute_options( $product->get_id(), $attribute ),
					'new_options'=> $this->get_new_attribute_options( $product->get_id(), $attribute ),
				);
			}
		}

		return $attributes;
    }

    /**
	 * Get attribute options.
	 *
	 * @param int   $product_id Product ID.
	 * @param array $attribute  Attribute data.
	 * @return array
	 */
	protected function get_attribute_options( $product_id, $attribute ) {


        if ( isset( $attribute['is_taxonomy'] ) && $attribute['is_taxonomy'] ) {
            return wc_get_product_terms( $product_id, $attribute['name'], array( 'fields' => 'names' ) );
		} elseif ( isset( $attribute['value'] ) ) {
            return array_map( 'trim', explode( '|', $attribute['value'] ) );
		}

		return array();
    }
	/**
	 * Get New attribute options.
	 *
	 * @param int   $product_id Product ID.
	 * @param array $attribute  Attribute data.
	 * @return array
	 */
	public function get_new_attribute_options( $product_id, $attribute ) {


        if ( isset( $attribute['is_taxonomy'] ) && $attribute['is_taxonomy'] ) {
			$value=wc_get_product_terms( $product_id, $attribute['name'], array( 'fields' => 'all' ) );
			$new_option=array();
			foreach($value as $val)
			{
				$image_preview = get_term_meta( $val->term_id, 'image_preview', true );
				$color_preview = get_term_meta( $val->term_id, 'color_preview', true );
				if( !empty($image_preview) ){
					$img_url = wp_get_attachment_image_src( $image_preview, 'ciyashop-brand-logo' );
				/*	$new_option[]=array(
									'name' => $val->name,
									'color'=>'',
									'image'=>$img_url[0]
								); */
				}
				else
				{
					$img_url="";
				}
				$new_option[]=array(
								'variation_name' => $val->name,
								'color'=>$color_preview,
								'image'=> ( isset( $img_url[0] ) ) ? $img_url[0] : '',
							);


			}
			return $new_option;

		} elseif ( isset( $attribute['value'] ) ) {
			$attributes = array_map( 'trim', explode( '|', $attribute['value'] ) );
			foreach($attributes as $att){
				$new_option[]=array(
								'variation_name' => $att,
								'color'			 => "",
								'image'          => ""
				);
			}
			return $new_option;
            //return array_map( 'trim', explode( '|', $attribute['value'] ) );
		}

		return array();
	}

    /**
	 * Get product attribute taxonomy name.
	 *
	 * @param  string     $slug    Taxonomy name.
	 * @param  WC_Product $product Product data.
	 * @return string
	 */
	protected function get_attribute_taxonomy_name( $slug, $product ) {
		$attributes = $product->get_attributes();

		if ( ! isset( $attributes[ $slug ] ) ) {
			return str_replace( 'pa_', '', $slug );
		}

		$attribute = $attributes[ $slug ];

		// Taxonomy attribute name.
		if ( $attribute->is_taxonomy() ) {
			$taxonomy = $attribute->get_taxonomy_object();
			return $taxonomy->attribute_label;
		}

		// Custom product attribute name.
		return $attribute->get_name();
    }

    /* get product data */

    public function get_products_data($product_id){
        pgs_woo_api_hook_remove_tax_in_price_html();//Remove include tax in price html

        $wcp = wc_get_product($product_id);
        $wce = new WC_Product_External($product_id);

        $rewards_message = '';
        $is_reward_points_active = pgs_woo_api_is_reward_points_active();
        if($is_reward_points_active){
            $rewards_Product = new PGS_WOO_API_RewardsController();
            $rewards_msg = $rewards_Product->get_single_product_rewards_message($wcp);
            if(isset($rewards_msg) && !empty($rewards_msg)){
                $rewards_message = $rewards_msg;
            }
        }

        $get_price = $wcp->get_price();
        $regular_price = $wcp->get_regular_price();
        $sale_price = $wcp->get_sale_price();
        $wc_tax_enabled = wc_tax_enabled();
        $tax_status =  'none';
        $tax_class = '';
        if($wc_tax_enabled){
            $tax_price = wc_get_price_to_display( $wcp );	//tax
            $price_including_tax = wc_get_price_including_tax( $wcp );
            $price_excluding_tax = wc_get_price_excluding_tax( $wcp );
            $tax_status =  $wcp->get_tax_status();
            $tax_class = $wcp->get_tax_class();
        }

        if($this->is_currency_switcher_active){
            $regular_price = $this->pgs_woo_api_update_currency_rate($regular_price);
            $sale_price = $this->pgs_woo_api_update_currency_rate($sale_price);
            $get_price = $this->pgs_woo_api_update_currency_rate($get_price);
            if($wc_tax_enabled){
                $tax_price = $this->pgs_woo_api_update_currency_rate($tax_price);
                $price_including_tax = $this->pgs_woo_api_update_currency_rate($price_including_tax);
                $price_excluding_tax = $this->pgs_woo_api_update_currency_rate($price_excluding_tax);
            }
        }
        $addition_info_html = '';
        $addition_info_data = array_filter( $wcp->get_attributes(), 'wc_attributes_array_filter_visible' );
        if ( $wcp && ( $wcp->has_attributes() || apply_filters( 'wc_product_enable_dimensions_display', $wcp->has_weight() || $wcp->has_dimensions() ) ) ) {
            $addition_info_html = $this->pgs_woo_api_get_addition_info_data($addition_info_data,$wcp);
        }

        $tax_price = (isset($tax_price))?$tax_price:'';
        $price_including_tax = (isset($price_including_tax))?$price_including_tax:'';
        $price_excluding_tax = (isset($price_excluding_tax))?$price_excluding_tax:'';

        $featured_video = (object)array();
        if($this->is_yith_featured_video_active){
            $featured_video = $this->pgs_woo_api_get_yith_featured_video($wcp,$product_id);
        }

        $data = array(
			'id' => $wcp->get_id(),
			'name' => $wcp->get_name(),
			'slug' => $wcp->get_slug(),
			'permalink' =>  $wcp->get_permalink(),
			'date_created' => wc_rest_prepare_date_response( $wcp->get_date_created(), false ),
			'date_created_gmt' => wc_rest_prepare_date_response( $wcp->get_date_created() ),
			'date_modified' =>wc_rest_prepare_date_response( $wcp->get_date_modified(), false ),
			'date_modified_gmt' => wc_rest_prepare_date_response( $wcp->get_date_modified() ),
			'type' => $wcp->get_type(),
			'status' => $wcp->get_status(),
			'featured' => $wcp->get_featured(),
			'catalog_visibility' => $wcp->get_catalog_visibility(),
			'description' => wpautop($wcp->get_description()),
			'short_description' => $wcp->get_short_description(),
			'sku' =>  $wcp->get_sku(),
			'price' =>  $get_price,
            'tax_price'=> $tax_price, //tax
            'price_excluding_tax' => $price_excluding_tax,
            'price_including_tax' => $price_including_tax,
			'regular_price' => $regular_price,
			'sale_price' => $sale_price,
			'date_on_sale_from' => wc_rest_prepare_date_response($wcp->get_date_on_sale_from()),
			'date_on_sale_from_gmt' => wc_rest_prepare_date_response($wcp->get_date_on_sale_from()),
			'date_on_sale_to' =>  wc_rest_prepare_date_response($wcp->get_date_on_sale_to()),
			'date_on_sale_to_gmt' =>  wc_rest_prepare_date_response($wcp->get_date_on_sale_to()),
			'price_html' => $wcp->get_price_html(),
			'on_sale' => $wcp->is_on_sale(),
			'purchasable' => $wcp->is_purchasable(),
			'total_sales' => $wcp->get_total_sales(),
			'virtual' => $wcp->get_virtual(),
			'downloadable' => $wcp->get_downloadable(),
			'downloads' => $wcp->get_downloads(),
			'download_limit' => $wcp->get_download_limit(),
			'download_expiry' => $wcp->get_download_expiry(),
			'external_url' => $wce->get_product_url(),
			'button_text' => $wce->get_button_text(),
			'tax_status' =>  $tax_status,
            'tax_class' => $tax_class,
			'manage_stock' => $wcp->get_manage_stock(),
			'stock_quantity' => $wcp->get_stock_quantity(),
			'in_stock' => $wcp->is_in_stock(),
			'backorders' => $wcp->get_backorders(),
			'backorders_allowed' => $wcp->backorders_allowed(),
			'backordered' => $wcp->backorders_allowed(),
			'sold_individually' => $wcp->get_sold_individually(),
			'weight' => $wcp->get_weight(),
			'dimensions'            => array(
				'length' => $wcp->get_length(),
				'width'  => $wcp->get_width(),
				'height' => $wcp->get_height(),
			),
			'shipping_required'     => $wcp->needs_shipping(),
			'shipping_taxable'      => $wcp->is_shipping_taxable(),
			'shipping_class'        => $wcp->get_shipping_class(),
			'shipping_class_id'     => $wcp->get_shipping_class_id(),
			'reviews_allowed'       => $wcp->get_reviews_allowed(),
			'average_rating'        => $wcp->get_average_rating(),
			'rating_count'          => $wcp->get_review_count(),
			'related_ids'           => array_map( 'absint', array_values( wc_get_related_products( $wcp->get_id() ) ) ),
			'upsell_ids'            => array_map( 'absint', $wcp->get_upsell_ids() ),
			'cross_sell_ids'        => array_map( 'absint', $wcp->get_cross_sell_ids() ),
			'parent_id'             => $wcp->get_parent_id(),
			'purchase_note'         => wpautop( do_shortcode( wp_kses_post( $wcp->get_purchase_note() ) ) ),
			'categories'            => $this->get_taxonomy_terms( $wcp ),
			'tags'                  => $this->get_taxonomy_terms( $wcp, 'tag' ),
			'images'                => $this->get_images( $wcp ),
			'app_thumbnail'         => $this->get_app_thumbnail($wcp),
            'attributes'            => $this->get_attributes( $wcp ),
			'default_attributes'    => $this->get_default_attributes( $wcp ),
			'variations'            => array(),
			'grouped_products'      => array(),
			'menu_order'            => $wcp->get_menu_order(),
			'meta_data'             => $wcp->get_meta_data(),
            'rewards_message'       => $rewards_message,
            'addition_info_html'    => (isset($addition_info_html) && !empty($addition_info_html))?$addition_info_html:'',
            'featured_video'        => $featured_video
        );

        // Add variations to variable products.
		if ( $wcp->is_type( 'variable' ) && $wcp->has_child() ) {
			$data['variations'] = $wcp->get_children();
		}

        // Add grouped products data.
		if ( $wcp->is_type( 'grouped' ) && $wcp->has_child() ) {
			$data['grouped_products'] = $wcp->get_children();
		}
        return $data;
    }

	 /**
	 * Get taxonomy terms.
	 *
	 * @param WC_Product $product  Product instance.
	 * @param string     $taxonomy Taxonomy slug.
	 * @return array
	 */
	public function get_taxonomy_terms( $product, $taxonomy = 'cat' ) {
		$terms = array();

		foreach ( wc_get_object_terms( $product->get_id(), 'product_' . $taxonomy ) as $term ) {
			$terms[] = array(
				'id'   => $term->term_id,
				'name' => $term->name,
				'slug' => $term->slug,
			);
		}

		return $terms;
	}

	 /**
	 * Get the images for a product or product variation.
	 *
	 * @param WC_Product|WC_Product_Variation $product Product instance.
	 * @return array
	 */
	public function get_images( $product ) {
		$images = array();
		$attachment_ids = array();

		// Add featured image.
		if ( has_post_thumbnail( $product->get_id() ) ) {
			$attachment_ids[] = $product->get_image_id();
		}

		// Add gallery images.
		$attachment_ids = array_merge( $attachment_ids, $product->get_gallery_image_ids() );

		// Build image data.
		foreach ( $attachment_ids as $position => $attachment_id ) {
			$attachment_post = get_post( $attachment_id );
			if ( is_null( $attachment_post ) ) {
				continue;
			}

			$attachment = wp_get_attachment_image_src( $attachment_id, apply_filters( 'pgs_woo_api_single_product_image', 'large' ) );
			if ( ! is_array( $attachment ) ) {
				continue;
			}


			$images[] = array(
				'id'                => (int) $attachment_id,
				'date_created'      => wc_rest_prepare_date_response( $attachment_post->post_date, false ),
				'date_created_gmt'  => wc_rest_prepare_date_response( strtotime( $attachment_post->post_date_gmt ) ),
				'date_modified'     => wc_rest_prepare_date_response( $attachment_post->post_modified, false ),
				'date_modified_gmt' => wc_rest_prepare_date_response( strtotime( $attachment_post->post_modified_gmt ) ),
				'src'               => current( $attachment ),
				'name'              => get_the_title( $attachment_id ),
				'alt'               => get_post_meta( $attachment_id, '_wp_attachment_image_alt', true ),
				'position'          => (int) $position,
			);
		}

		// Set a placeholder image if the product has no images set.
		if ( empty( $images ) ) {
			$images[] = array(
				'id'                => 0,
				'date_created'      => wc_rest_prepare_date_response( current_time( 'mysql' ), false ), // Default to now.
				'date_created_gmt'  => wc_rest_prepare_date_response( current_time( 'timestamp', true ) ), // Default to now.
				'date_modified'     => wc_rest_prepare_date_response( current_time( 'mysql' ), false ),
				'date_modified_gmt' => wc_rest_prepare_date_response( current_time( 'timestamp', true ) ),
				'src'               => wc_placeholder_img_src(),
				'name'              => __( 'Placeholder', 'pgs-woo-api' ),
				'alt'               => __( 'Placeholder', 'pgs-woo-api' ),
				'position'          => 0,
			);
		}

		return $images;
	}
	/**
	 * Get default attributes.
	 *
	 * @param WC_Product $product Product instance.
	 * @return array
	 */
	public function get_default_attributes( $product ) {
		$default = array();

		if ( $product->is_type( 'variable' ) ) {
			foreach ( array_filter( (array) $product->get_default_attributes(), 'strlen' ) as $key => $value ) {
				if ( 0 === strpos( $key, 'pa_' ) ) {
					$default[] = array(
						'id'     => wc_attribute_taxonomy_id_by_name( $key ),
						'name'   => $this->get_attribute_taxonomy_name( $key, $product ),
						'option' => $value,
					);
				} else {
					$default[] = array(
						'id'     => 0,
						'name'   => $this->get_attribute_taxonomy_name( $key, $product ),
						'option' => $value,
					);
				}
			}
		}

		return $default;
	}
	public function pgs_woo_api_get_addition_info_data($attributes,$product){
        $display_dimensions = apply_filters( 'pgs_woo_api_wc_product_enable_dimensions_display', $product->has_weight() || $product->has_dimensions() );
        $html = '';
        $html .= '<table class="shop_attributes">';
    	if ( $display_dimensions && $product->has_weight() ) :
    		$html .= '<tr>';
    			$html .= '<th>'.esc_html__( 'Weight', 'pgs-woo-api' ).'</th>';
    			$html .= '<td class="product_weight">'.esc_html( wc_format_weight( $product->get_weight() ) ).'</td>';
    		$html .= '</tr>';
    	endif;

    	if ( $display_dimensions && $product->has_dimensions() ) :
    		$html .= '<tr>';
    			$html .= '<th>'.esc_html__( 'Dimensions', 'pgs-woo-api' ).'</th>';
    			$html .= '<td class="product_dimensions">'.esc_html( wc_format_dimensions( $product->get_dimensions( false ) ) ).'</td>';
    		$html .= '</tr>';
    	endif;

        foreach ( $attributes as $attribute ) :
    		$html .= '<tr>';
    			$html .= '<th>'.wc_attribute_label( $attribute->get_name() ).'</th>';
    			$html .= '<td>';
    				$values = array();

    				if ( $attribute->is_taxonomy() ) {
    					$attribute_taxonomy = $attribute->get_taxonomy_object();
    					$attribute_values = wc_get_product_terms( $product->get_id(), $attribute->get_name(), array( 'fields' => 'all' ) );

    					foreach ( $attribute_values as $attribute_value ) {
    						$value_name = esc_html( $attribute_value->name );

    						if ( $attribute_taxonomy->attribute_public ) {
    							$values[] = $value_name;
    						} else {
    							$values[] = $value_name;
    						}
    					}
    				} else {
    					$values = $attribute->get_options();

    					foreach ( $values as &$value ) {
    						$value = esc_html( $value );
    					}
    				}

    				$html .= wptexturize( implode( ', ', $values ) );
    			$html .= '</td>';
    		$html .= '</tr>';
	   endforeach;
       $html .= '</table>';
       return $html;
    }

	// Is pincode plugin activated.
	public function pgs_woo_api_is_pincode_plugin(){
		$return = array('status' => 'disable');
		if ( function_exists( 'pincode_plugin_activation' ) ) {
			$return['status'] = 'enable';
			global $wpdb;

			// Current site prefix
			$tbprefix = $wpdb->prefix;
			$free_plugin = $tbprefix.'check_pincode_p';

			if($wpdb->get_var( "show tables like '$free_plugin'") == $free_plugin){
				update_option('active_pincode_check', '' );
			}

			// Enable pro plugin check
			$active_pincode_check = get_option('active_pincode_check');

			if( $active_pincode_check )
			{
				$tblname = "pincode_setting_pro";
			} else {
				$tblname = "pincode_setting_p";
			}

			$sql   = "SELECT *";
			$sql  .= " FROM ".$tbprefix.$tblname;
			$result = $wpdb->get_results($sql);
			if( $result && $active_pincode_check ) {
				$default_setting = $result[0];
				$setting_option['del_help_text'] = $default_setting->del_help_text;
				$setting_option['cod_help_text'] = $default_setting->cod_help_text;
				$setting_option['cod_available_msg'] = $default_setting->cod_msg1;
				$setting_option['cod_not_available_msg'] = $default_setting->cod_msg2;
				$setting_option['error_msg_check_pincode'] =  $default_setting->error_msg;
				$setting_option['del_saturday'] =  $default_setting->s_s;
				$setting_option['del_sunday'] =  $default_setting->s_s1;

				$setting_option['show_product_page'] = get_option('show_product_page');
				$del_label =  get_option( 'woo_pin_check_del_label' );
				if($del_label == '' ) {
					$setting_option['del_data_label'] = esc_html__( 'Delivered By', 'pgs-woo-api' );
				} else {
					$setting_option['del_data_label'] = $del_label;
				}

				$cod_label =  get_option( 'woo_pin_check_cod_label' );
				if($cod_label == '' ) {
					$setting_option['cod_data_label'] = esc_html__( 'Cash On Delivery', 'pgs-woo-api' );
				} else {
					$setting_option['cod_data_label'] = $cod_label;
				}

				$availableat_text =  get_option( 'availableat_text' );
				if($availableat_text == '' ) {
					$setting_option['availableat_text'] = esc_html__( 'Available at', 'pgs-woo-api' );
				} else {
					$setting_option['availableat_text'] = $availableat_text;
				}

				$setting_option['error_msg_blank'] =  get_option( 'woo_pin_check_error_msg_b' );

				$setting_option['pincode_placeholder_txt'] =  get_option( 'woo_pin_check_checkpin_text' );

				$setting_option['show_state_on_product'] =  get_option( 'woo_pin_check_show_s_on_pro' );
				$setting_option['show_city_on_product'] =  get_option( 'woo_pin_check_show_c_on_pro' );
				$setting_option['show_estimate_on_product'] = get_option('show_deli_est');
				$setting_option['show_cod_on_product'] = get_option('show_cod_a');
				$return['setting_options'] = $setting_option;
			}
		}
		return $return;
	}

	function pgs_woo_api_app_get_web_view_page_list(){
		$pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
		$web_view_list = array();
		if(!empty($pgs_woo_api_home_option))
		{
			if(isset($pgs_woo_api_home_option['web_view_pages']) && !empty($pgs_woo_api_home_option['web_view_pages']) )
			{
				$web_view_data = $pgs_woo_api_home_option['web_view_pages'];
				foreach($web_view_data as $web_view){
					$web_view['web_view_pages_page_id'] = get_permalink($web_view['web_view_pages_page_id']);
					$web_view_list[] = $web_view;
				}
			}
		}
		return $web_view_list;
	}

	/**
	 * Set verification status.
	 *
	 * URL : https://your-website/wp-json/pgs-woo-api/v1/verified
	 *
	 * @param WP_REST_Request $request {
	 *     	 Request used to generate the response.
	 *
	 *       @type int|string $purchase_code           Purchase code.
	 *       @type int|string $is_purchase_verified    Purchase verification status.
	 * }
	 * @return WP_Error|WP_REST_Response
	 */
	public function set_verificaion_status( WP_REST_Request $request ) {

		$this->wp_rest_request = $request;
		$this->params          = $this->wp_rest_request->get_params();

		if ( isset( $this->params['user_id'] ) && ! empty( $this->params['user_id'] ) ) {
			$this->user_id = absint( $this->params['user_id'] );
		}

		if ( isset( $this->params['app-ver'] ) && ! empty( $this->params['app-ver'] ) ) {
    		$this->app_ver = $this->params['app-ver'];
    	}

		$params  = $this->params;
		$data    = array(
			'status' => 'error',
		);

		$purchase_code        = ( isset( $params['purchase_code'] ) && ! empty( $params['purchase_code'] ) ) ? sanitize_text_field( wp_unslash( $params['purchase_code'] ) ) : '';
		$is_purchase_verified = ( isset( $params['is_purchase_verified'] ) && ! empty( $params['is_purchase_verified'] ) ) ? sanitize_text_field( wp_unslash( $params['is_purchase_verified'] ) ) : 'no';

		if ( empty( $purchase_code ) ) {
			$data['message'] = esc_html__( 'Purchse code is empty.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		if ( empty( $is_purchase_verified ) ) {
			$data['message'] = esc_html__( 'Verification status is empty.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		if ( ! in_array( $is_purchase_verified, array( 'yes', 'no' ), true ) ) {
			$data['message'] = esc_html__( 'Invalid verification status.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		delete_option( "pgs_woo_api-is_verified-{$purchase_code}" );

		$is_verified_update_status = update_option( "pgs_woo_api-is_verified-{$purchase_code}", $is_purchase_verified );
		if ( ! $is_verified_update_status ) {
			$data['message'] = esc_html__( 'Something went wrong. Unable to update verification status.', 'pgs-woo-api' );
			return new WP_REST_Response( $data, 200 );
		}

		$data = array(
			'status'  => 'success',
			'message' => esc_html__( 'Verification status updated successfully.', 'pgs-woo-api' ),
			'data'    => array(),
		);

		$data['is_user_exists'] = 'not_provided';
		if ( version_compare( $this->app_ver, '4.4.0', '>=' ) && ! empty( $this->user_id ) ) {
			$is_user_exists = $this->is_user_exists( $this->user_id, $this->params, $this->wp_rest_request );

			$data['is_user_exists'] = $is_user_exists;
		}

		return new WP_REST_Response( $data, 200 );
	}

	/**
	 * Get verification status.
	 *
	 * @param WP_REST_Request $request Request used to generate the response.
	 * @return string
	 */
	public function get_verificaion_status( $request ) {
		$verification_status = 'no';

		$purchase_code  = ( isset( $request['purchase_code'] ) && ! empty( $request['purchase_code'] ) ) ? sanitize_text_field( wp_unslash( $request['purchase_code'] ) ) : '';

		if ( ! empty( $purchase_code ) ) {
			$is_verified = get_option( "pgs_woo_api-is_verified-{$purchase_code}" );
			if ( $is_verified && 'yes' === $is_verified ) {
				$verification_status = 'yes';
			}
		}

		return $verification_status;
	}

 }
new PGS_WOO_API_HomeController;
