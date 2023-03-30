<?php
/**
 * Envato PGS WOO API Setup Wizard Class
 *
 * Takes new users through some basic steps to setup their codecanyon plugins.
 *
 * @author      dtbaker
 * @author      vburlak
 * @package     envato_wizard
 * @version     1.2.4
 *
 *
 * 1.2.0 - added custom_logo
 * 1.2.1 - ignore post revisioins
 * 1.2.2 - elementor widget data replace on import
 * 1.2.3 - auto export of content.
 * 1.2.4 - fix category menu links
 *
 * Based off the WooThemes installer.
 *
 *
 *
 */
if ( ! defined( 'ABSPATH' ) ) {
	exit;
}

if ( ! class_exists( 'Envato_PGS_API_Plugin_Setup_Wizard' ) ) {
	/**
	 * Envato_PGS_API_Plugin_Setup_Wizard class
	 */
	class Envato_PGS_API_Plugin_Setup_Wizard {

		/**
		 * The class version number.
		 *
		 * @since 1.1.1
		 * @access private
		 *
		 * @var string
		 */
		protected $version = '1.2.4';

		protected $pgs_plugin_data = '';

		/** @var string Current plugin name, used as namespace in actions. */
		protected $pgs_plugin_name = '';

		/** @var string Current Step */
		protected $step = '';

		/** @var array Steps for the setup wizard */
		protected $steps = array();

		/**
		 * Relative plugin path
		 *
		 * @since 1.1.2
		 *
		 * @var string
		 */
		protected $plugin_path = '';

		/**
		 * Relative plugin url for this plugin folder, used when enquing scripts
		 *
		 * @since 1.1.2
		 *
		 * @var string
		 */
		protected $plugin_url = '';

		/**
		 * The slug name to refer to this menu
		 *
		 * @since 1.1.1
		 *
		 * @var string
		 */
		protected $page_slug;

		/**
		 * TGMPA instance storage
		 *
		 * @var object
		 */
		protected $tgmpa_instance;

		/**
		 * TGMPA Menu slug
		 *
		 * @var string
		 */
		protected $tgmpa_menu_slug = 'tgmpa-install-plugins';

		/**
		 * TGMPA Menu url
		 *
		 * @var string
		 */
		protected $tgmpa_url = 'themes.php?page=tgmpa-install-plugins';

		/**
		 * The slug name for the parent menu
		 *
		 * @since 1.1.2
		 *
		 * @var string
		 */
		protected $page_parent;

		/**
		 * Complete URL to Setup Wizard
		 *
		 * @since 1.1.2
		 *
		 * @var string
		 */
		public $page_url;

		protected $sample_datas;

		protected $themeforest_profile_url;


		/**
		 * Holds the current instance of the plugin manager
		 *
		 * @since 1.1.3
		 * @var Envato_PGS_API_Plugin_Setup_Wizard
		 */
		private static $instance = null;

		public $api_url;

		/**
		 * @since 1.1.3
		 *
		 * @return Envato_PGS_API_Plugin_Setup_Wizard
		 */
		public static function get_instance() {
			if ( ! self::$instance ) {
				self::$instance = new self;
			}

			return self::$instance;
		}

		/**
		 * A dummy constructor to prevent this class from being loaded more than once.
		 *
		 * @see Envato_PGS_API_Plugin_Setup_Wizard::instance()
		 *
		 * @since 1.1.1
		 * @access private
		 */
		public function __construct() {
			$this->init_globals();
			$this->init_actions();
		}

		/**
		 * Get the default style. Can be overriden by theme init scripts.
		 *
		 * @see Envato_PGS_API_Plugin_Setup_Wizard::instance()
		 *
		 * @since 1.1.7
		 * @access public
		 */
		public function get_default_theme_style() {
			return 'pink';
		}

		/**
		 * Get the default style. Can be overriden by theme init scripts.
		 *
		 * @see Envato_PGS_API_Plugin_Setup_Wizard::instance()
		 *
		 * @since 1.1.9
		 * @access public
		 */
		public function get_header_logo_width() {
			return apply_filters( 'envato_pag_plugin_setup_wizard_header_logo_width', '200px' );
		}


		/**
		 * Get the default style. Can be overriden by theme init scripts.
		 *
		 * @see Envato_PGS_API_Plugin_Setup_Wizard::instance()
		 *
		 * @since 1.1.9
		 * @access public
		 */
		public function pgs_woo_api_get_logo_image() {
			$image_url =  trailingslashit($this->plugin_url).'images/logo.png';
			return apply_filters( 'pgs_woo_api_envato_setup_logo_image', $image_url );
		}

		/**
		 * Setup the class globals.
		 *
		 * @since 1.1.1
		 * @access public
		 */
		public function init_globals() {
			$current_child_theme   = false;
            $current_plugin = pgs_woo_api_get_plugin_data();

			$this->pgs_plugin_data         = $current_plugin;
			$this->pgs_plugin_slug         = apply_filters( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_slug', sanitize_title($current_plugin['Name']));
			$this->pgs_plugin_name         = apply_filters( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_name', str_replace('-', '_', $this->pgs_plugin_slug) );
            $this->page_slug               = apply_filters( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_page_slug', $this->pgs_plugin_name . '-setup' );
			$this->parent_slug             = apply_filters( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_parent_slug', 'pgs-woo-api-settings' );
			$this->page_url                = apply_filters( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_page_url', ( $this->parent_slug !== '' ) ? 'admin.php?page=' . $this->page_slug : 'themes.php?page=' . $this->page_slug );
			$this->api_url                 = apply_filters( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_api_url', '' );
			$this->sample_datas            = apply_filters( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_styles', array() );
			$this->themeforest_profile_url = apply_filters( 'envato_pgs_api_plugin_setup_wizard_themeforest_profile_url', array() );

			//set relative plugin path url
			$this->plugin_path = trailingslashit( $this->cleanFilePath( dirname( __FILE__ ) ) );
			$relative_url      = str_replace( $this->cleanFilePath( PGS_API_PATH ), '', $this->plugin_path );
			$this->plugin_url  = PGS_API_URL.'inc/setup-wizard/envato_setup/';
			add_action( "admin_footer", array( $this, "pgs_woo_api_warning_alert_templates_cls" ) );
		}

		/**
		 * Template file for show alert contet for show notice alert
		 */
		function pgs_woo_api_warning_alert_templates_cls() {
			if( isset( $_GET['page'] ) && $_GET['page'] == 'pgs_woo_api-setup' ) {
				include_once trailingslashit(PGS_API_PATH) . "template/warning-alert/pgs-woo-api-warning-alert.php";
			}
		}

		/**
		 * Setup the hooks, actions and filters.
		 *
		 * @uses add_action() To add actions.
		 * @uses add_filter() To add filters.
		 *
		 * @since 1.1.1
		 * @access public
		 */
		public function init_actions() {
            if ( apply_filters( $this->pgs_plugin_name . '_enable_setup_wizard', true ) && current_user_can( 'manage_options' ) ) {

				if(!is_child_theme()){
					add_action( 'after_switch_theme', array( $this, 'switch_theme' ) );
				}

				if ( class_exists( 'TGM_Plugin_Activation' ) && isset( $GLOBALS['pgs_api_tgmpa'] ) ) {
					add_action( 'init', array( $this, 'get_tgmpa_instanse' ), 30 );
					add_action( 'init', array( $this, 'set_tgmpa_url' ), 40 );
				}

				add_action( 'admin_menu', array( $this, 'admin_menus' ) );
				//add_action( 'admin_enqueue_scripts', array( $this, 'enqueue_scripts' ) );
				add_action( 'admin_init', array( $this, 'admin_redirects' ), 30 );
				add_action( 'admin_init', array( $this, 'init_wizard_steps' ), 30 );
				add_action( 'admin_init', array( $this, 'setup_wizard' ), 30 );
				add_filter( 'tgmpa_load', array( $this, 'tgmpa_load' ), 10, 1 );
				add_action( 'wp_ajax_envato_setup_plugins', array( $this, 'ajax_plugins' ) );
				add_action( 'admin_init', array( $this, 'pgs_woo_api_call_try_now') );
				//add_action( 'wp_ajax_pgs_woo_api_call_try_now_new', array( $this, 'pgs_woo_api_call_try_now') );
            }
			#add_action( 'upgrader_post_install', array( $this, 'upgrader_post_install' ), 10, 2 );
		}

		public function pgs_woo_api_call_try_now(){

			if( isset($_POST['action']) && $_POST['action'] == "pgs_woo_api_call_try_now" && $_GET['page'] == "pgs_woo_api-setup" ){
				$action_url = admin_url( 'admin.php?page=pgs_woo_api-setup&step=activate' );
				if ( ! wp_verify_nonce( $_POST['call_try_now_nonce'], 'pgs_woo_api_call_try_now_security' ) ) {
					$import_status_data = array(
						'success'     => false,
						'message'     => esc_html__( 'Please verify your purchase key!' , 'pgs-woo-api' ),
						'action'      => $action_url
					);
				} else {
					$import_status_data = array(
						'success'     => false,
						'message'     => esc_html__( 'Please verify your purchase key!' , 'pgs-woo-api' ),
						'action'      => $action_url
					);
				}
				wp_send_json( $import_status_data );
				exit();
			}
		}

		/**
		 * After a theme update we clear the setup_complete option. This prompts the user to visit the update page again.
		 *
		 * @since 1.1.8
		 * @access public
		 */
		public function upgrader_post_install( $return, $theme ) {
			if ( is_wp_error( $return ) ) {
				return $return;
			}
			if ( $theme != get_stylesheet() ) {
				return $return;
			}
			update_option( 'pgs_woo_api_envato_setup_complete', false );

			return $return;
		}


		public function enqueue_scripts() {
		}

		public function tgmpa_load( $status ) {
			return is_admin() || current_user_can( 'install_themes' );
		}

		public function switch_theme() {
			set_transient( '_' . $this->pgs_plugin_name . '_activation_redirect', 1 );
		}

		public function admin_redirects() {
			$after_theme_switch = $this->after_theme_switch();
			if(  isset($after_theme_switch) && $after_theme_switch == 'wizard' ){
				ob_start();
				if ( ! get_transient( '_' . $this->pgs_plugin_name . '_activation_redirect' ) || get_option( 'pgs_woo_api_envato_setup_complete', false ) ) {
					return;
				}
				delete_transient( '_' . $this->pgs_plugin_name . '_activation_redirect' );
				wp_safe_redirect( admin_url( $this->page_url ) );
				exit;
			}
		}

		/**
		 * Get configured TGMPA instance
		 *
		 * @access public
		 * @since 1.1.2
		 */
		public function get_tgmpa_instanse() {
			$this->tgmpa_instance = call_user_func( array( get_class( $GLOBALS['pgs_api_tgmpa'] ), 'get_instance' ) );
		}

		/**
		 * Update $tgmpa_menu_slug and $tgmpa_parent_slug from TGMPA instance
		 *
		 * @access public
		 * @since 1.1.2
		 */
		public function set_tgmpa_url() {

			$this->tgmpa_menu_slug = ( property_exists( $this->tgmpa_instance, 'menu' ) ) ? $this->tgmpa_instance->menu : $this->tgmpa_menu_slug;
			$this->tgmpa_menu_slug = apply_filters( $this->pgs_plugin_name . '_theme_setup_wizard_tgmpa_menu_slug', $this->tgmpa_menu_slug );

			$tgmpa_parent_slug = ( property_exists( $this->tgmpa_instance, 'parent_slug' ) && $this->tgmpa_instance->parent_slug !== 'themes.php' ) ? 'admin.php' : 'themes.php';

			$this->tgmpa_url = apply_filters( $this->pgs_plugin_name . '_theme_setup_wizard_tgmpa_url', $tgmpa_parent_slug . '?page=' . $this->tgmpa_menu_slug );

		}

		/**
		 * Add admin menus/screens.
		 */
		public function admin_menus() {

			if ( $this->is_submenu_page() ) {
				//prevent Theme Check warning about "themes should use add_theme_page for adding admin pages"
				$add_subpage_function = 'add_submenu' . '_page';
				$add_subpage_function( $this->parent_slug, esc_html__( 'Setup Wizard', 'pgs-woo-api' ), esc_html__( 'Setup Wizard', 'pgs-woo-api' ), 'manage_options', $this->page_slug, array(
					$this,
					'setup_wizard',
				) );
			} else {
                add_theme_page( esc_html__( 'Setup Wizard', 'pgs-woo-api' ), esc_html__( 'Setup Wizard', 'pgs-woo-api' ), 'manage_options', $this->page_slug, array(
					$this,
					'setup_wizard',
				) );
			}
		}


		/**
		 * Setup steps.
		 *
		 * @since 1.1.1
		 * @access public
		 * @return array
		 */
		public function init_wizard_steps() {
			$purchase_token = pgs_woo_api_is_activated();

			$this->steps = array(
				'introduction' => array(
					'name'    => esc_html__( 'Introduction', 'pgs-woo-api' ),
					'view'    => array( $this, 'envato_setup_introduction' ),
					'handler' => array( $this, 'envato_setup_introduction_save' ),
				),
			);


			$this->steps['activate'] = array(
				'name'    => esc_html__( 'Activate', 'pgs-woo-api' ),
				'view'    => array( $this, 'envato_setup_activate' ),
				'handler' => array( $this, 'envato_setup_activate_save' ),
			);

			/*$this->steps['customize'] = array(
				'name'    => esc_html__( 'Child Theme', 'pgs-woo-api' ),
				'view'    => array( $this, 'envato_setup_customize' ),
				'handler' => '',
			);*/

			if ( class_exists( 'TGM_Plugin_Activation' ) && isset( $GLOBALS['pgs_api_tgmpa'] ) ) {
				$this->steps['default_plugins'] = array(
					'name'    => esc_html__( 'Plugins', 'pgs-woo-api' ),
					'view'    => array( $this, 'envato_setup_default_plugins' ),
					'handler' => '',
				);
			}
			$this->steps['default_content'] = array(
				'name'    => esc_html__( 'Content', 'pgs-woo-api' ),
				'view'    => array( $this, 'envato_setup_default_content' ),
				'handler' => '',
			);
			$this->steps['help_support']    = array(
				'name'    => esc_html__( 'Support', 'pgs-woo-api' ),
				'view'    => array( $this, 'envato_setup_help_support' ),
				'handler' => '',
			);
			$this->steps['final']      = array(
				'name'    => esc_html__( 'Ready!', 'pgs-woo-api' ),
				'view'    => array( $this, 'envato_setup_ready' ),
				'handler' => '',
			);

			$this->steps = apply_filters( $this->pgs_plugin_name . '_plugin_setup_wizard_steps', $this->steps );
        }

		/**
		 * Show the setup wizard
		 */
		public function setup_wizard() {
			if ( empty( $_GET['page'] ) || $this->page_slug !== $_GET['page'] ) {
				return;
			}
			ob_end_clean();

			$this->step = isset( $_GET['step'] ) ? sanitize_key( $_GET['step'] ) : current( array_keys( $this->steps ) );

			wp_register_script( 'jquery-blockui', $this->plugin_url . 'js/jquery.blockUI.js', array( 'jquery' ), '2.70', true );
			wp_register_script( 'envato-setup', $this->plugin_url . 'js/envato-setup.js', array(
				'jquery',
				'jquery-blockui',
			), $this->version );

			wp_register_style( 'jquery-confirm-bootstrap-cls' , PGS_API_URL.'css/jquery-confirm/jquery-confirm-bootstrap.css' );
    		wp_register_style( 'jquery-confirm-cls', PGS_API_URL.'css/jquery-confirm/jquery-confirm.css' );
			wp_register_script( 'jquery-confirm-cls', PGS_API_URL.'js/jquery-confirm/jquery-confirm.js', array('jquery'), false, false );
			wp_register_script('pgs-woo-api-confirm-custom-js-cls', PGS_API_URL.'js/confirm-custom.js', array('jquery-confirm-cls','wp-util'), false, false );
            $auth_token = PGS_WOO_API_Support::pgs_woo_api_verify_plugin();
            $ciyashop_native = 'android';
            $purchase_key_android = get_option('pgs_woo_api_plugin_android_purchase_key');
            $purchase_key_ios = get_option('pgs_woo_api_plugin_ios_purchase_key');
            if ( empty($auth_token) ) {
                $ciyashop_native = 'android';
            } else {
                if($purchase_key_android != ''){
                    $ciyashop_native = 'android';
                } elseif($purchase_key_ios != ''){
                    $ciyashop_native = 'ios';
                }
            }
            $ciyashop_native = ( isset($_POST['ciyashop_native']))?$_POST['ciyashop_native']:$ciyashop_native;
            $activated_with = pgs_woo_api_activated_with();
            wp_localize_script( 'envato-setup', 'envato_setup_params', array(
				'tgm_plugin_nonce' => array(
					'update'  => wp_create_nonce( 'tgmpa-update' ),
					'install' => wp_create_nonce( 'tgmpa-install' ),
				),
				'tgm_bulk_url'     => admin_url( $this->tgmpa_url ),
				'ajaxurl'          => admin_url( 'admin-ajax.php' ),
				'wpnonce'          => wp_create_nonce( 'envato_setup_nonce' ),
				'verify_text'      => esc_html__( '...verifying', 'pgs-woo-api' ),
                'ciyashop_native'  => $ciyashop_native,
                'purchased_android' => ( $activated_with['purchased_android'] ) ? $activated_with['purchased_android'] : false,
                'purchased_ios' => ( $activated_with['purchased_ios'] ) ? $activated_with['purchased_ios'] : false
            ) );

			wp_enqueue_style( 'envato-setup', $this->plugin_url . 'css/envato-setup.css', array(
				'wp-admin',
				'dashicons',
				'install',
			), $this->version );

			$sts = pgs_woo_api_wp_warning_alert();
			if( $sts ){
				wp_enqueue_style( 'jquery-confirm-bootstrap-cls' );
				wp_enqueue_style( 'jquery-confirm-cls' );
				wp_enqueue_script( 'jquery-confirm-cls' );
				wp_localize_script( 'pgs-woo-api-confirm-custom-js-cls', 'pgs_app_confirm_object', array(
					'alert_title'                      => esc_html__( 'Warning', 'pgs-woo-api' ),
					'alert_cancel'                     => esc_html__( 'Cancel', 'pgs-woo-api' )
				));
				wp_enqueue_script( 'pgs-woo-api-confirm-custom-js-cls' );
			}
			//enqueue style for admin notices
			wp_enqueue_style( 'wp-admin' );

			wp_enqueue_media();
			wp_enqueue_script( 'media' );

			ob_start();
			$this->setup_wizard_header();
			$this->setup_wizard_steps();
			$show_content = true;
			?>
			<div class="envato-setup-content envato-setup-content-step-<?php echo esc_attr($this->step);?>">
				<?php
				if ( ! empty( $_REQUEST['save_step'] ) && isset( $this->steps[ $this->step ]['handler'] ) ) {
					$show_content = call_user_func( $this->steps[ $this->step ]['handler'] );
				}
				if ( $show_content ) {
					$this->setup_wizard_content();
				}
				?>
			</div>
			<?php
			$this->setup_wizard_footer();
			exit;
		}

		public function get_step_link( $step ) {
			return add_query_arg( 'step', $step, admin_url( 'admin.php?page=' . $this->page_slug ) );
		}

		public function get_next_step_link() {
			$keys = array_keys( $this->steps );

			return add_query_arg( 'step', $keys[ array_search( $this->step, array_keys( $this->steps ) ) + 1 ], remove_query_arg( 'translation_updated' ) );
		}

		/**
		 * Setup Wizard Header
		 */
		public function setup_wizard_header() {
			?>
			<!DOCTYPE html>
			<html xmlns="http://www.w3.org/1999/xhtml" <?php language_attributes(); ?>>
			<head>
				<meta name="viewport" content="width=device-width"/>
				<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
				<?php
				// avoid theme check issues.
				echo wp_kses( '<ti'.'tle>' . esc_html__( 'Plugin &rsaquo; Setup Wizard', 'pgs-woo-api' ) . '</ti'.'tle>', pgs_woo_api_allowed_html('title') );

				wp_print_scripts( 'envato-setup' );
				do_action( 'admin_print_styles' );
				do_action( 'admin_print_scripts' );
				// do_action( 'admin_head' );
				?>
			</head>
			<body class="envato-setup wp-core-ui envato-setup-step-<?php echo esc_attr($this->step);?>">
				<h1 id="wc-logo">
					<?php
					$header_logo = sprintf( '<img class="site-logo" src="%s" alt="%s" style="width:%s; height:auto" />',
						( $this->pgs_woo_api_get_logo_image() ) ? $this->pgs_woo_api_get_logo_image() : trailingslashit($this->plugin_url).'img/logo.png',
						$this->pgs_plugin_data['Name'],
						$this->get_header_logo_width()
					);
                    echo wp_kses( $header_logo, pgs_woo_api_allowed_html( array('img')) );
					?>
				</h1>
			<?php
		}

		/**
		 * Setup Wizard Footer
		 */
		public function setup_wizard_footer() {
					if ( 'final' === $this->step ) {
						?>
						<a class="wc-return-to-dashboard" href="<?php echo esc_url( admin_url() ); ?>">
							<?php esc_html_e( 'Return to the WordPress Dashboard', 'pgs-woo-api' ); ?>
						</a>
						<?php
					}
					?>
					<p class="copyrights">
						<?php
						$copyright = '';
						if( $this->pgs_plugin_data['Author'] ){
							$copyright = sprintf(
								// Translators: %s is the theme author link.
								esc_html__( '&copy; Created by %s', 'pgs-woo-api' ),
								( $this->pgs_plugin_data['AuthorURI'] ) ? sprintf( '<a href="%s" target="_blank" rel="noopener">%s</a>', $this->pgs_plugin_data['AuthorURI'], $this->pgs_plugin_data['Author'] ) : $this->pgs_plugin_data['Author']
							);
						}
						$copyright = apply_filters( 'envato_setup_wizard_footer_copyright', $copyright, $this->pgs_plugin_data );
						if( $copyright ){
							echo wp_kses( $copyright, pgs_woo_api_allowed_html('a','span','i') );
						}
						?>
					</p>
				</body>
				<?php
				@do_action( 'admin_footer' ); // this was spitting out some errors in some admin templates. quick @ fix until I have time to find out what's causing errors.
				do_action( 'admin_print_footer_scripts' );
				?>
			</html>
			<?php
		}

		/**
		 * Output the steps
		 */
		public function setup_wizard_steps() {
			$ouput_steps = $this->steps;
			array_shift( $ouput_steps );
			?>
			<ol class="envato-setup-steps">
				<?php
				foreach ( $ouput_steps as $step_key => $step ) :
					$class = 'envato-setup-step';
					$class .= ' envato-setup-step-'.$step_key;

					$show_link = false;
					if ( $step_key === $this->step ) {
						$class .= ' active';
					} elseif ( array_search( $this->step, array_keys( $this->steps ) ) > array_search( $step_key, array_keys( $this->steps ) ) ) {
						$class .= ' done';
						$show_link = true;
					}
					?>
					<li class="<?php echo esc_attr($class);?>"><?php
						if ( $show_link ) {
							?>
							<a href="<?php echo esc_url( $this->get_step_link( $step_key ) ); ?>"><?php echo esc_html( $step['name'] ); ?></a>
							<?php
						} else {
							echo esc_html( $step['name'] );
						}
						?></li>
				<?php endforeach; ?>
			</ol>
			<?php
		}

		/**
		 * Output the content for the current step
		 */
		public function setup_wizard_content() {
			isset( $this->steps[ $this->step ] ) ? call_user_func( $this->steps[ $this->step ]['view'] ) : false;
		}

		/**
		 * Introduction step
		 */
		public function envato_setup_introduction() {

			if ( false && isset( $_REQUEST['debug'] ) ) {
				?>
				<pre>
					<?php
					// debug inserting a particular post so we can see what's going on
					$post_type = 'post';
					$post_id   = 1; // debug this particular import post id.
					$all_data  = $this->_get_json( 'default.json' );
					if ( ! $post_type || ! isset( $all_data[ $post_type ] ) ) {
						echo sprintf( esc_html__( "Post type %s not found.", 'pgs-woo-api' ), $post_type);
					} else {
						echo sprintf( esc_html__( "Looking for post id %s", 'pgs-woo-api' ), $post_id)."\n";
						foreach ( $all_data[ $post_type ] as $post_data ) {

							if ( $post_data['post_id'] == $post_id ) {
								print_r( $post_data );
							}
						}
					}
					print_r( $this->logs );
					?>
				</pre>
				<?php
			} else if ( get_option( 'pgs_woo_api_envato_setup_complete', false ) ) {
				?>
				<h1><?php printf( esc_html__( 'Welcome to the steps for setting the %s plugin.', 'pgs-woo-api' ), $this->pgs_plugin_data['Name'] ); ?></h1>
				<p><?php esc_html_e( 'It seems that you have already been through the setup medium. Below are some choices:', 'pgs-woo-api' ); ?></p>
				<ul>
					<li>
						<a href="<?php echo esc_url( $this->get_next_step_link() ); ?>" class="button button-large button-primary button-next button-large">
							<?php esc_html_e( 'Run Setup Wizard Again', 'pgs-woo-api' ); ?>
						</a>
					</li>
				</ul>
				<p class="envato-setup-actions step">
					<a href="<?php echo esc_url( wp_get_referer() && ! strpos( wp_get_referer(), 'update.php' ) ? wp_get_referer() : admin_url( '' ) ); ?>"
					   class="button button-large"><?php esc_html_e( 'Cancel', 'pgs-woo-api' ); ?>
				   </a>
				</p>
				<?php
			} else {
				?>
				<h1><?php printf( esc_html__( 'Welcome to Ciyashop Native ( %s ) Setup Wizard', 'pgs-woo-api' ), $this->pgs_plugin_data['Name'] ); ?></h1>
				<p><?php printf( esc_html__( 'Thank you for choosing Ciyashop Native ( %s ) Application.', 'pgs-woo-api' ), $this->pgs_plugin_data['Name'] ); ?></p>
				<p><?php printf( esc_html__( 'This setup wizard will help you to refresh and configure your application home screen with a new layout. You will have Content, and Plugins installed in 5-10 minutes (depending on your server configuration). ', 'pgs-woo-api' ), $this->pgs_plugin_data['Name'] ); ?></p>
				<p><?php esc_html_e( 'No time right now? If you do not want to go through the wizard, you can skip, and get back to WordPress dashboard. Come back any time to continue!', 'pgs-woo-api' ); ?></p>
				<p class="envato-setup-actions step">
					<a href="<?php echo esc_url( $this->get_next_step_link() ); ?>"
					   class="button button-large button-primary button-next button-active"><?php esc_html_e( 'Let\'s Go!', 'pgs-woo-api' ); ?></a>
					<a href="<?php echo esc_url( wp_get_referer() && ! strpos( wp_get_referer(), 'update.php' ) ? wp_get_referer() : admin_url( '' ) ); ?>"
					   class="button button-large"><?php esc_html_e( 'Not right now', 'pgs-woo-api' ); ?></a>
				</p>
				<?php
			}
		}

		public function filter_options( $options ) {
			return $options;
		}

		/**
		 *
		 * Handles save button from welcome page. This is to perform tasks when the setup wizard has already been run. E.g. reset defaults
		 *
		 * @since 1.2.5
		 */
		public function envato_setup_introduction_save() {

			check_admin_referer( 'envato-setup' );

			if ( ! empty( $_POST['reset-font-defaults'] ) && $_POST['reset-font-defaults'] == 'yes' ) {

				// clear font options
				update_option( 'tt_font_theme_options', array() );

				// reset site color
				remove_theme_mod( 'dtbwp_site_color' );

				if ( class_exists( 'dtbwp_customize_save_hook' ) ) {
					$site_color_defaults = new dtbwp_customize_save_hook();
					$site_color_defaults->save_color_options();
				}

				$file_name = ('/style.custom.css');
				if ( file_exists( $file_name ) ) {
					require_once( ABSPATH . 'wp-admin/includes/file.php' );
					WP_Filesystem();
					global $wp_filesystem;
					$wp_filesystem->put_contents( $file_name, '' );
				}
				?>
				<p>
					<strong><?php esc_html_e( 'Options have been reset. Please go to Appearance > Customize in the WordPress backend.', 'pgs-woo-api' ); ?></strong>
				</p>
				<?php
				return true;
			}

			return false;
		}

		/**
		 * Payments Step
		 */
		public function envato_setup_activate() {
			$auth_token = PGS_WOO_API_Support::pgs_woo_api_verify_plugin();
            ?>
			<h1><?php esc_html_e( 'Activate Plugin', 'pgs-woo-api' ); ?></h1>
			<?php if(empty($auth_token)){?>
                <p class="lead"><?php esc_html_e( 'Enter purchase code to activate your plugin.', 'pgs-woo-api' ); ?></p>
                <?php
            }
			$slug = basename( get_template_directory() );

			$output = '';$prefix = '';$default_chk_android = 'checked="checked"';$default_chk_ios='';
			if( isset($_POST['ciyashop_native']) && $_POST['ciyashop_native'] == "android"){
                $prefix = "android";
                $error = get_site_transient("pgs_woo_api_auth_notice_$prefix");
                $default_chk_android = 'checked="checked"';$default_chk_ios='';
            } elseif( isset($_POST['ciyashop_native']) && $_POST['ciyashop_native'] == "ios"){
                $prefix = "ios";
                $error = get_site_transient("pgs_woo_api_auth_notice_$prefix");
                $default_chk_ios = 'checked="checked"';$default_chk_android='';
    		}

            //get notice
            $purchase_key_android = get_option('pgs_woo_api_plugin_android_purchase_key');
            $purchase_key_ios = get_option('pgs_woo_api_plugin_ios_purchase_key');
            if ( empty($auth_token) ) {
                $purchase_key_android = '';
                $purchase_key_ios = '';
            } else {
                if($purchase_key_android != ''){
                    $default_chk_android = 'checked="checked"';$default_chk_ios='';
                } elseif($purchase_key_ios != ''){
                    $default_chk_ios = 'checked="checked"';$default_chk_android='';
                }
                if( isset($_POST['ciyashop_native']) && $_POST['ciyashop_native'] == "android"){
                    $default_chk_android = 'checked="checked"';$default_chk_ios='';
                } elseif( isset($_POST['ciyashop_native']) && $_POST['ciyashop_native'] == "ios"){
                    $default_chk_ios = 'checked="checked"';$default_chk_android='';
        		}
            }
			?>
			<form class="ciyashop_activate_pgs_plugin" method="post" action="">
			<?php
			//display notices
			if ( !empty($notices) ) {
				echo '<div class="notice-'.$notices['notice_type'].' notice-alt notice-large"><p>' . $notices['notice'] . '</p></div>';
			}
			$purchase_disabled_android = '';
            $purchase_disabled_ios = '';
			$activated_with = pgs_woo_api_activated_with();
            if( isset($activated_with['purchased_android']) && !empty($activated_with['purchased_android'])){
                $purchase_disabled_android = 'disabled';
            }
            if( isset($activated_with['purchased_ios']) && !empty($activated_with['purchased_ios'])){
                $purchase_disabled_ios = 'disabled';
            }
            ?>
				<?php wp_nonce_field( 'purchase_code_activation', 'purchase_code_nonce' ); ?>
				<div class="native-app-lbl">
                    <label>CiyaShop Native Application based on WooCommerce</label>
                </div>
                <div class="native-app-lbl-rb">
                    <label>Android <input class="ciyashop-native-app" type="radio" name="ciyashop_native" value="android" <?php echo $default_chk_android?> /></label>
                    <label>iOS <input class="ciyashop-native-app" type="radio" name="ciyashop_native" value="ios" <?php echo $default_chk_ios?>/></label>
                </div>
                <?php
                if( ! empty($_POST['pgs_woo_api_verify_plugin']["purchase_key_$prefix"]) && !empty($auth_token) && empty($error) ) {?>
            		<div class="pgs-woo-api-admin-important-success-notice notice-success notice-alt notice-large">
            			<?php echo esc_html( get_site_transient("pgs_woo_api_pgs_auth_msg_$prefix") ); ?>
            		</div>
                    <?php
            		delete_site_transient("pgs_woo_api_pgs_auth_msg_$prefix");
            	}
                if ( !empty($auth_token) && $prefix == '' ) {?>
                    <div class="notice-success notice-alt notice-large"><p>Purchase code successfully verified.</p></div>
                    <?php
                }
                if( empty($_POST['pgs_woo_api_verify_plugin']["purchase_key_$prefix"]) && empty($auth_token) ) {
                    $error = esc_html__('Please enter product purchase key to activate plugin.', 'pgs-woo-api');
        		}

                if( !empty($error) ) {
                     ?>
                        <div class="pgs-woo-api-admin-important-notice notice-warning notice-alt notice-large">
                            <?php
                            echo esc_html($error);
                            delete_site_transient("pgs_woo_api_auth_notice_$prefix");
                            ?>
                        </div>
                    <?php
                }?>
                <div class="android-frm-field-group">
                    <input type="hidden" name="pgs_woo_api_nonce" value="<?php echo wp_create_nonce('pgs-woo-api-verify-token');?>" />
            		<input type="hidden" name="pgs_woo_api_verify_plugin[item_key_android]" value="c7ec1dc95001d57cdedfe122569648dc" />
                    <input type="text" name="pgs_woo_api_verify_plugin[purchase_key_android]" value="<?php echo !empty($purchase_key_android) ? esc_attr( $purchase_key_android ) : ''; ?>" <?php echo esc_attr($purchase_disabled_android);?> placeholder="<?php esc_attr_e('Purchase code ( e.g. 9g2b13fa-10aa-2267-883a-9201a94cf9b5 )', 'pgs-woo-api');?>" />
                </div>
                <div class="ios-frm-field-group" style="display: none;">
                    <input type="hidden" name="pgs_woo_api_nonce" value="<?php echo wp_create_nonce('pgs-woo-api-verify-token');?>" />
                    <input type="hidden" name="pgs_woo_api_verify_plugin[item_key_ios]" value="7884626eb301b0f657bb23894fd2dbfe" />
                    <input type="text" name="pgs_woo_api_verify_plugin[purchase_key_ios]" value="<?php echo !empty($purchase_key_ios) ? esc_attr( $purchase_key_ios ) : ''; ?>" <?php echo esc_attr($purchase_disabled_ios);?> placeholder="<?php esc_attr_e('iOS Purchase code ( e.g. 9g2b13fa-10aa-2267-883a-9201a94cf9b5 )', 'pgs-woo-api');?>" />
                </div>

				<div class="activation-instructions">
					<h3><?php esc_html_e( 'Instructions to find the Purchase Code', 'pgs-woo-api' );?></h3>
					<ol>
						<li><?php esc_html_e( 'Log into your Envato Market account.', 'pgs-woo-api' );?></li>
						<li><?php esc_html_e( 'Hover the mouse over your username at the top of the screen.', 'pgs-woo-api' );?></li>
						<li><?php esc_html_e( 'Click \'Downloads\' from the drop-down menu.', 'pgs-woo-api' );?></li>
						<li><?php printf(
							// Translators: %s is the ThemeForest Item Support Policy link.
							wp_kses( __( 'Click \'License certificate & purchase code\' (available as PDF or text file). Click <a href="%s" target="_blank">here</a> for more information.', 'pgs-woo-api' ),
								pgs_woo_api_allowed_html(array('a'))
							),
							'https://help.market.envato.com/hc/en-us/articles/202822600-Where-Is-My-Purchase-Code'
						);?></li>
					</ol>
				</div>
				<p class="envato-setup-actions step">
					<?php
					if ( empty( $purchase_key_android ) && empty( $purchase_key_ios ) ) {
						?>
						<input type="submit" class="activate-code-btn button button-large button-next button-primary" value="<?php esc_attr_e( 'Activate', 'pgs-woo-api' );?>"/>
						<?php
					} else{
                        if( (isset($_POST['ciyashop_native']) && $_POST['ciyashop_native'] == "android") && empty($purchase_key_android)){
                            ?>
                            <input type="submit" class="activate-code-btn button button-large button-next button-primary" value="<?php esc_attr_e( 'Activate', 'pgs-woo-api' );?>"/>
                            <?php
                        } elseif( (isset($_POST['ciyashop_native']) && $_POST['ciyashop_native'] == "ios") && empty($purchase_key_ios)){
                            ?>
                            <input type="submit" class="activate-code-btn button button-large button-next button-primary" value="<?php esc_attr_e( 'Activate', 'pgs-woo-api' );?>"/>
                            <?php
                		}
                        ?>
						<a href="<?php echo esc_url( $this->get_next_step_link() );?>" class="continue-btn button button-primary button-large button-next">
							<?php esc_html_e( 'Continue', 'pgs-woo-api' );?>
						</a>
						<?php
					}
					?>
				</p>
			</form>
			<?php
			wp_nonce_field( 'envato-setup' );
		}

		/**
		 * Payments Step save
		 */
		public function envato_setup_activate_save() {
			check_admin_referer( 'envato-setup' );

			// redirect to our custom login URL to get a copy of this token.
			$url = $this->get_oauth_login_url( $this->get_step_link( 'updates' ) );

			wp_redirect( esc_url_raw( $url ) );
			exit;
		}

		private function _get_plugins( $version = false ) {
			$instance = call_user_func( array( get_class( $GLOBALS['pgs_api_tgmpa'] ), 'get_instance' ) );

			$plugins  = array(
				'all'      => array(), // Meaning: all plugins which still have open actions.
				'install'  => array(),
				'update'   => array(),
				'activate' => array(),
			);

			foreach ( $instance->plugins as $slug => $plugin ) {
                if ( $this->is_plugin_check_active( $slug ) && false === $instance->does_plugin_have_update( $slug ) ) {
					// No need to display plugins if they are installed, up-to-date and active.
					continue;
				} else {

                    $plugins['all'][ $slug ] = $plugin;
					if ( ! $instance->is_plugin_installed( $slug ) ) {
						$plugins['install'][ $slug ] = $plugin;
					} else {
						if ( false !== $instance->does_plugin_have_update( $slug ) ) {
							$plugins['update'][ $slug ] = $plugin;
						}

						if ( $instance->can_plugin_activate( $slug ) ) {
							$plugins['activate'][ $slug ] = $plugin;
						}
					}
				}
			}
            $pgs_woo_api_plugins = array();
            foreach($plugins['all'] as $key => $plugin){
			    if($plugin['slug'] == "rest-api"){
			        $pgs_woo_api_plugins[$key] = $plugins['all'][$key];
			    } elseif($plugin['slug'] == "woocommerce"){
			        $pgs_woo_api_plugins[$key] = $plugins['all'][$key];
			    } elseif($plugin['slug'] == "rest-api-oauth1"){
			        $pgs_woo_api_plugins[$key] = $plugins['all'][$key];
			    } else {
			        unset($plugins['all'][$key]);
			    }
			}
            if(!empty($pgs_woo_api_plugins)){
                unset($plugins['all']);
                $plugins['all'] = $pgs_woo_api_plugins;
            }
			return $plugins;
		}

		public function is_plugin_check_active( $slug ) {
			$instance = call_user_func( array( get_class( $GLOBALS['pgs_api_tgmpa'] ), 'get_instance' ) );
            return ( ( ! empty( $instance->plugins[ $slug ]['is_callable'] ) && is_callable( $instance->plugins[ $slug ]['is_callable'] ) ) || pgs_woo_api_widzard_check_plugin_active( $instance->plugins[ $slug ]['file_path'] ) );
		}


		/**
		 * Page setup
		 */
		public function envato_setup_default_plugins() {

			tgmpa_load_bulk_installer();


			// install plugins with TGM.
			if ( ! class_exists( 'TGM_Plugin_Activation' ) || ! isset( $GLOBALS['pgs_api_tgmpa'] ) ) {
				die( 'Failed to find TGM' );
			}
			$url     = wp_nonce_url( add_query_arg( array( 'plugins' => 'go' ) ), 'envato-setup' );
			$plugins = $this->_get_plugins();

			// copied from TGM
			$method = ''; // Leave blank so WP_Filesystem can populate it as necessary.
			$fields = array_keys( $_POST ); // Extra fields to pass to WP_Filesystem.

			if ( false === ( $creds = request_filesystem_credentials( esc_url_raw( $url ), $method, false, false, $fields ) ) ) {
				return true; // Stop the normal page form from displaying, credential request form will be shown.
			}

			// Now we have some credentials, setup WP_Filesystem.
			if ( ! WP_Filesystem( $creds ) ) {
				// Our credentials were no good, ask the user for them again.
				request_filesystem_credentials( esc_url_raw( $url ), $method, true, false, $fields );

				return true;
			}

			$version_import = false;

			if( isset( $_GET['version'] ) && isset( $this->sample_datas[$_GET['version']] ) ) {
				$version_import = $_GET['version'];
			}

			/* If we arrive here, we have the filesystem */
			?>
			<h1><?php esc_html_e( 'Default Plugins', 'pgs-woo-api' );?></h1>
			<form method="post" class="plugins-form" data-version="<?php echo esc_attr( $version_import ); ?>">

				<?php
				$plugins = $this->_get_plugins( $version_import );
                $required = array_filter($plugins['all'], function($el) {
					return $el['required'];
				});

				$version_plugins = ( ! empty( $this->sample_datas[ $version_import ]['plugins'] ) ) ? $this->sample_datas[ $version_import ]['plugins'] : array();

				$for_version = array_filter($plugins['all'], function($el) use($version_plugins) {
					return in_array( $el['slug'], array_merge($version_plugins) );
				});

				$recommended = array_filter($plugins['all'], function($el) use( $for_version ) {
					return ( ! $el['required'] && ! isset( $for_version[ $el['slug'] ] ) );
				});


                if ( count( $plugins['all'] ) ) {
                    ?>
					<p><?php esc_html_e( 'The following plugins can be installed for some supplemented features to your website for Application API:', 'pgs-woo-api' ); ?></p>
					<ul class="envato-wizard-plugins">
						<?php
						$required_core = array();
						if ( ! empty( $required ) ){
							?>
							<li class="plugins-title"><?php esc_html_e( 'Required','pgs-woo-api' );?></li>
							<?php
							$this->_list_plugins( $required, $plugins, false, 'required' );
						}
						if ( ! empty( $for_version ) ){
							?>
							<li class="plugins-title"><?php esc_html_e( 'Needed for this version', 'pgs-woo-api' ); ?></li>
							<?php
							$this->_list_plugins( $for_version, $plugins, true, 'for_version' );
						}
						?>
					</ul>
					<?php
				} else {
						?>
						<p><strong><?php esc_html_e( 'Good news! All plugins are already installed and up to date. Please continue.', 'pgs-woo-api' );?></strong></p>
						<?php

				}
				?>

				<p><?php esc_html_e( 'Please, note that every external plugin can affect your website loading speed. You can add and remove plugins later on from within WordPress.', 'pgs-woo-api' ); ?></p>

				<p class="envato-setup-actions step">
					<a href="<?php echo esc_url( $this->get_next_step_link() ); ?>" class="button button-large button-primary button-next button-active" data-callback="install_plugins"><?php esc_html_e( 'Continue', 'pgs-woo-api' ); ?></a>
					<?php wp_nonce_field( 'envato-setup' ); ?>
				</p>
			</form>
			<?php
		}

		private function _list_plugins( $plugins, $all, $checked = false, $plugin_type = 'recommended' ) {
			foreach ($plugins as $slug => $plugin) {
				$this->_plugin_list_item( $slug, $plugin, $all, $checked, $plugin_type );
			}
		}

		private function _plugin_list_item( $slug, $plugin, $plugins, $checked = false, $plugin_type ) {
			$message_strings = array(
				'Activate'                      => esc_html__( 'Activate', 'pgs-woo-api' ),
				'Activation required'           => esc_html__( 'Activation required', 'pgs-woo-api' ),
				'Install'                       => esc_html__( 'Install', 'pgs-woo-api' ),
				'Installation required'         => esc_html__( 'Installation required', 'pgs-woo-api' ),
				'Update and Activate'           => esc_html__( 'Update and Activate', 'pgs-woo-api' ),
				'Update and Activation required'=> esc_html__( 'Update and Activation required', 'pgs-woo-api' ),
				'Update required'               => esc_html__( 'Update required', 'pgs-woo-api' ),
				'Update'                        => esc_html__( 'Update', 'pgs-woo-api' ),
			);
			?>
			<li data-slug="<?php echo esc_attr( $slug ); ?>" class="plugin-to-install">
				<label for="plugin-import[<?php echo esc_attr($slug);?>]">
					<?php
					echo sprintf( '<input type="checkbox" name="%s" id="%s"%s>',
						"plugin-import[$slug]",
						"plugin-import[$slug]",
						checked( ( (isset($plugin['checked_in_wizard']) && $plugin['checked_in_wizard'] != '') || $checked ), true, false )
					);

					$plugin_details = ( (isset($plugin['details_url']) && $plugin['details_url'] != '') ? sprintf( ' (<a href="%s" target="_blank" rel="noopener">%s</a>)', esc_url( $plugin['details_url'] ), esc_html__( 'View details', 'pgs-woo-api' ) ) : '' );
					echo sprintf( '<span class="plugin-title">%s</span>%s',
						esc_html( trim($plugin['name']) ),
						$plugin_details
					);
					?>
					<span class="status"></span>
					<span class="plugin-action">
						<?php
						$keys = array();
						if( $plugin_type == 'required' ){
							if ( isset( $plugins['install'][ $slug ] ) ) {
								$keys[] = 'Installation';
							}
							if ( isset( $plugins['update'][ $slug ] ) ) {
								$keys[] = 'Update';
							}
							if ( isset( $plugins['activate'][ $slug ] ) ) {
								$keys[] = 'Activation';
							}
							echo esc_html($message_strings[ trim(implode( ' and ', $keys ) . ' required') ]);
						}else{
							if ( isset( $plugins['install'][ $slug ] ) ) {
								$keys[] = 'Install';
							}
							if ( isset( $plugins['update'][ $slug ] ) ) {
								$keys[] = 'Update';
							}
							if ( isset( $plugins['activate'][ $slug ] ) ) {
								$keys[] = 'Activate';
							}
							echo esc_html($message_strings[ trim(implode( ' and ', $keys )) ]);
						}
						?>
					</span>
					<div class="spinner"></div>
				</label>
			</li>
			<?php
		}

		public function ajax_plugins() {
			if ( ! check_ajax_referer( 'envato_setup_nonce', 'wpnonce' ) || empty( $_POST['slug'] ) ) {
				wp_send_json_error( array( 'error' => 1, 'message' => esc_html__( 'No Slug Found', 'pgs-woo-api' ) ) );
			}
			$json = array();

			// send back some json we use to hit up TGM
			$plugins = $this->_get_plugins();

			// what are we doing with this plugin?
			foreach ( $plugins['activate'] as $slug => $plugin ) {
				if ( $_POST['slug'] == $slug ) {
					$json = array(
						'url'           => admin_url( $this->tgmpa_url ),
						'plugin'        => array( $slug ),
						'tgmpa-page'    => $this->tgmpa_menu_slug,
						'plugin_status' => 'all',
						'_wpnonce'      => wp_create_nonce( 'bulk-plugins' ),
						'action'        => 'tgmpa-bulk-activate',
						'action2'       => - 1,
						'message'       => esc_html__( 'Activating Plugin', 'pgs-woo-api' ),
					);
					break;
				}
			}
			foreach ( $plugins['update'] as $slug => $plugin ) {
				if ( $_POST['slug'] == $slug ) {
					$json = array(
						'url'           => admin_url( $this->tgmpa_url ),
						'plugin'        => array( $slug ),
						'tgmpa-page'    => $this->tgmpa_menu_slug,
						'plugin_status' => 'all',
						'_wpnonce'      => wp_create_nonce( 'bulk-plugins' ),
						'action'        => 'tgmpa-bulk-update',
						'action2'       => - 1,
						'message'       => esc_html__( 'Updating Plugin', 'pgs-woo-api' ),
					);
					break;
				}
			}
			foreach ( $plugins['install'] as $slug => $plugin ) {
				if ( $_POST['slug'] == $slug ) {
					$json = array(
						'url'           => admin_url( $this->tgmpa_url ),
						'plugin'        => array( $slug ),
						'tgmpa-page'    => $this->tgmpa_menu_slug,
						'plugin_status' => 'all',
						'_wpnonce'      => wp_create_nonce( 'bulk-plugins' ),
						'action'        => 'tgmpa-bulk-install',
						'action2'       => - 1,
						'message'       => esc_html__( 'Installing Plugin', 'pgs-woo-api' ),
					);
					break;
				}
			}

			if ( $json ) {
				$json['hash'] = md5( serialize( $json ) ); // used for checking if duplicates happen, move to next plugin
				wp_send_json( $json );
			} else {
				wp_send_json( array( 'done' => 1, 'message' => esc_html__( 'Success', 'pgs-woo-api' ) ) );
			}
			exit;

		}


		/**
		 * Page setup
		 */
		public function envato_setup_default_content() {
            $auth_token = PGS_WOO_API_Support::pgs_woo_api_verify_plugin();
			$plugins = $this->_get_plugins();
			$required = array_filter($plugins['all'], function($el) {
				return $el['required'];
			});

			$required_names = array_reduce($required, function($carry = array(), $item) {
				$carry[] = $item['name'];
				return $carry;
			});

			$sample_datas_imported = array();

			$sample_datas = $this->sample_datas;
			?>
			<h1><?php esc_html_e( 'Default Content', 'pgs-woo-api' ); ?></h1>
			<form method="post">
				<p><?php esc_html_e( 'Select the most suitable option from the below mentioned list of variants to complete the default content for your Application. Also, choose the appropriate pages to be imported from the list. The WordPress dashboard handles this content once you import them.', 'pgs-woo-api' ); ?></p>
				<?php
				if( !empty($required_names) ){
					?>
					<div class="content-missing-plugin-notice-wrapper">
						<div class="content-missing-plugin-notice"><?php esc_html_e( 'One, or more, required plugins (listed below) not installed or activated. Some features may not work correctly, i.e., Sample Data import.', 'pgs-woo-api' ); ?></div>
						<ul class="content-missing-plugins">
							<?php
							array_walk($required_names, function($item, $key) {
								echo "<li class='content-missing-plugin'>$item</li>";
							});
							?>
						</ul>
					</div>
					<?php
				}

				if ( empty($auth_token) ) {
					?>
					<div class="plugin-activation-notice">
						<p><strong><?php esc_html_e( 'Please acivate plugin using Purchase Code to display sample data list.', 'pgs-woo-api' );?></strong></p>
					</div>
					<?php
				}

				if( !pgs_woo_api_token_is_activated() ){
					$sample_datas = array();
				}
				?>
				<div class="sample-contents-wrapper clearfix">
					<div class="sample-contents">
						<?php
						$i = 0;
						$sample_data_path = PGS_API_PATH.'inc/sample_data';
						$sample_data_url  = PGS_API_URL.'inc/sample_data';


                        $imported_samples = array();
                        $pgs_woo_api_sample_data_arr = get_option( 'pgs_woo_api_default_sample_data_arr' );
    					if(isset($pgs_woo_api_sample_data_arr) && !empty($pgs_woo_api_sample_data_arr)){
                            $imported_samples = json_decode($pgs_woo_api_sample_data_arr);
                        }
                        $sample_data_active = get_option( 'pgs_woo_api_default_sample_data_active' );
                        $first_demo_to_import = '';
						foreach ($sample_datas as $sample_data_k => $sample_data) {
							$sample_data_classes = array('sample-content');
							$sample_data_classes[] = 'sample-content-'.$sample_data_k;
                            $imported = false;
							if( in_array($sample_data['id'], $imported_samples) ){
								$imported = true;
								if(!empty($sample_data_active)){
								    if($sample_data_active == $sample_data['id']){
                                        $sample_data_classes[] = 'sample-content-active';
                                    }
								} else {
								    $sample_data_classes[] = 'pgs-woo-api-imported';
								}
							} else{
								$i++;
								if( $i==1 ){
								    $first_demo_to_import = $sample_data['id'];
									if(empty($sample_data_active)){
								        $sample_data_classes[] = 'sample-content-active';
                                    }
								}
							}
							$preview_img_path = trailingslashit(trailingslashit($sample_data_path).$sample_data['id']).'preview.jpg';
							$preview_img_url = trailingslashit(trailingslashit($sample_data_url).$sample_data['id']).'preview.jpg';


                            $sample_data_classes = implode( ' ', array_filter( array_unique( $sample_data_classes ) ) );

                            ?>
							<div class="<?php echo esc_attr($sample_data_classes);?>" data-version="<?php echo esc_attr($sample_data_k);?>">
								<div class="sample-content-view">
									<?php
									if( file_exists($preview_img_path) ){
										?>
										<div class="sample-content-thumb">
											<img src="<?php echo esc_url($preview_img_url);?>" alt="<?php echo esc_attr($sample_data['name']);?>"/>
										</div>
										<?php
									}else{
										?>
										<div class="sample-content-thumb sample-content-thumb-blank">
											<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAyAAAAJYCAQAAAAwf0r7AAAGrUlEQVR42u3VIQEAAAzDsM+/6dPh4URCSXMAMIgEABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYCAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYiAQAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgABgIABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAICBAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAYCAAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgAGAgABgIAAYCgIEAYCAAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBAICBAGAgABgIAAYCgIEAgIEAYCAAGAgABgKAgQCAgQBgIAAYCAAGAoCBSACAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAGAgAGAoCBAGAgABgIAAYCAAYCgIEAYCAAGAgABgIABgKAgQBgIAAYCAAGAgAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIAAYCAAYCAAGAoCBAGAgABgIABgIAAYCgIEAYCAAGAgAGAgABgKAgQBgIABgIAAYCAAGAoCBAGAgANAeRqACWRdZgjsAAAAASUVORK5CYII=" alt="<?php echo esc_attr($sample_data['name']);?>"/>
										</div>
										<?php
									}
									?>
									<span class="sample-content-thumb-title"><?php echo esc_html( $sample_data['name'] ); ?></span>
								    <?php
									if($imported == true){
									?>
										<!--span class="sample-status">
											<?php //echo esc_html("Installed", "pgs-woo-api");?>
										</span-->
									<?php
									}
									?>

                                </div>
								<div class="sample-content-title-wrap">
									<h2 class="sample-content-title"><?php echo esc_html( $sample_data['name'] ); ?></h2>
									<?php
									if( isset($sample_data['preview_url']) && $sample_data['preview_url'] != '' ){
										?>
										<a href="<?php echo esc_url( $sample_data['preview_url'] ); ?>" target="_blank" rel="noopener" class="live-preview-button button button-primary button-small">
											<?php esc_html_e('Live Preview', 'pgs-woo-api' ); ?>
										</a>
										<?php
									}
									?>
								</div>
							</div>
							<?php
						}
						?>
					</div>
				</div>

				<input type="hidden" name="import-id" id="import-id" value="<?php echo esc_attr( $first_demo_to_import );?>">
				<?php wp_nonce_field( 'pgs_woo_api_sample_data_security', 'sample_import_nonce' );?>

				<p class="envato-setup-actions step">
					<a href="<?php echo esc_url( $this->get_next_step_link() ); ?>" class="button button-large button-primary button-next button-active" data-callback="install_content"><?php esc_html_e( 'Continue', 'pgs-woo-api' ); ?></a>
					<a href="<?php echo esc_url( $this->get_next_step_link() ); ?>" class="button button-large"><?php esc_html_e( 'Skip this step', 'pgs-woo-api' ); ?></a>
					<?php wp_nonce_field( 'envato-setup' ); ?>
				</p>
			</form>
			<?php
		}



		private function _imported_term_id( $original_term_id, $new_term_id = false ) {
			$terms = get_transient( 'importtermids' );
			if ( ! is_array( $terms ) ) {
				$terms = array();
			}
			if ( $new_term_id ) {
				if ( ! isset( $terms[ $original_term_id ] ) ) {
					$this->log( 'Insert old TERM ID ' . $original_term_id . ' as new TERM ID: ' . $new_term_id );
				} else if ( $terms[ $original_term_id ] != $new_term_id ) {
					$this->error( 'Replacement OLD TERM ID ' . $original_term_id . ' overwritten by new TERM ID: ' . $new_term_id );
				}
				$terms[ $original_term_id ] = $new_term_id;
				set_transient( 'importtermids', $terms, 60 * 60 * 24 );
			} else if ( $original_term_id && isset( $terms[ $original_term_id ] ) ) {
				return $terms[ $original_term_id ];
			}

			return false;
		}


		public function vc_post( $post_id = false ) {

			$vc_post_ids = get_transient( 'import_vc_posts' );
			if ( ! is_array( $vc_post_ids ) ) {
				$vc_post_ids = array();
			}
			if ( $post_id ) {
				$vc_post_ids[ $post_id ] = $post_id;
				set_transient( 'import_vc_posts', $vc_post_ids, 60 * 60 * 24 );
			} else {

				$this->log( 'Processing vc pages 2: ' );

				return;
				if ( class_exists( 'Vc_Manager' ) && class_exists( 'Vc_Post_Admin' ) ) {
					$this->log( $vc_post_ids );
					$vc_manager = Vc_Manager::getInstance();
					$vc_base    = $vc_manager->vc();
					$post_admin = new Vc_Post_Admin();
					foreach ( $vc_post_ids as $vc_post_id ) {
						$this->log( 'Save ' . $vc_post_id );
						$vc_base->buildShortcodesCustomCss( $vc_post_id );
						$post_admin->save( $vc_post_id );
						$post_admin->setSettings( $vc_post_id );
						//twice? bug?
						$vc_base->buildShortcodesCustomCss( $vc_post_id );
						$post_admin->save( $vc_post_id );
						$post_admin->setSettings( $vc_post_id );
					}
				}
			}

		}

		private function _imported_post_id( $original_id = false, $new_id = false ) {
			if ( is_array( $original_id ) || is_object( $original_id ) ) {
				return false;
			}
			$post_ids = get_transient( 'importpostids' );
			if ( ! is_array( $post_ids ) ) {
				$post_ids = array();
			}
			if ( $new_id ) {
				if ( ! isset( $post_ids[ $original_id ] ) ) {
					$this->log( 'Insert old ID ' . $original_id . ' as new ID: ' . $new_id );
				} else if ( $post_ids[ $original_id ] != $new_id ) {
					$this->error( 'Replacement OLD ID ' . $original_id . ' overwritten by new ID: ' . $new_id );
				}
				$post_ids[ $original_id ] = $new_id;
				set_transient( 'importpostids', $post_ids, 60 * 60 * 24 );
			} else if ( $original_id && isset( $post_ids[ $original_id ] ) ) {
				return $post_ids[ $original_id ];
			} else if ( $original_id === false ) {
				return $post_ids;
			}

			return false;
		}

		private function _post_orphans( $original_id = false, $missing_parent_id = false ) {
			$post_ids = get_transient( 'postorphans' );
			if ( ! is_array( $post_ids ) ) {
				$post_ids = array();
			}
			if ( $missing_parent_id ) {
				$post_ids[ $original_id ] = $missing_parent_id;
				set_transient( 'postorphans', $post_ids, 60 * 60 * 24 );
			} else if ( $original_id && isset( $post_ids[ $original_id ] ) ) {
				return $post_ids[ $original_id ];
			} else if ( $original_id === false ) {
				return $post_ids;
			}

			return false;
		}

		private function _cleanup_imported_ids() {
			// loop over all attachments and assign the correct post ids to those attachments.

		}

		private $delay_posts = array();

		private function _delay_post_process( $post_type, $post_data ) {
			if ( ! isset( $this->delay_posts[ $post_type ] ) ) {
				$this->delay_posts[ $post_type ] = array();
			}
			$this->delay_posts[ $post_type ][ $post_data['post_id'] ] = $post_data;

		}


		// return the difference in length between two strings
		public function cmpr_strlen( $a, $b ) {
			return strlen( $b ) - strlen( $a );
		}

		private function _parse_gallery_shortcode_content($content){
			// we have to format the post content. rewriting images and gallery stuff
			$replace      = $this->_imported_post_id();
			$urls_replace = array();
			foreach ( $replace as $key => $val ) {
				if ( $key && $val && ! is_numeric( $key ) && ! is_numeric( $val ) ) {
					$urls_replace[ $key ] = $val;
				}
			}
			if ( $urls_replace ) {
				uksort( $urls_replace, array( &$this, 'cmpr_strlen' ) );
				foreach ( $urls_replace as $from_url => $to_url ) {
					$content = str_replace( $from_url, $to_url, $content );
				}
			}
			if ( preg_match_all( '#\[gallery[^\]]*\]#', $content, $matches ) ) {
				foreach ( $matches[0] as $match_id => $string ) {
					if ( preg_match( '#ids="([^"]+)"#', $string, $ids_matches ) ) {
						$ids = explode( ',', $ids_matches[1] );
						foreach ( $ids as $key => $val ) {
							$new_id = $val ? $this->_imported_post_id( $val ) : false;
							if ( ! $new_id ) {
								unset( $ids[ $key ] );
							} else {
								$ids[ $key ] = $new_id;
							}
						}
						$new_ids                   = implode( ',', $ids );
						$content = str_replace( $ids_matches[0], 'ids="' . $new_ids . '"', $content );
					}
				}
			}
			// contact form 7 id fixes.
			if ( preg_match_all( '#\[contact-form-7[^\]]*\]#', $content, $matches ) ) {
				foreach ( $matches[0] as $match_id => $string ) {
					if ( preg_match( '#id="(\d+)"#', $string, $id_match ) ) {
						$new_id = $this->_imported_post_id( $id_match[1] );
						if ( $new_id ) {
							$content = str_replace( $id_match[0], 'id="' . $new_id . '"', $content );
						} else {
							// no imported ID found. remove this entry.
							$content = str_replace( $matches[0], '(insert contact form here)', $content );
						}
					}
				}
			}
			return $content;
		}

		private function _elementor_id_import( &$item, $key ) {
			if ( $key == 'id' && ! empty( $item ) && is_numeric( $item ) ) {
				// check if this has been imported before
				$new_meta_val = $this->_imported_post_id( $item );
				if ( $new_meta_val ) {
					$item = $new_meta_val;
				}
			}
			if ( $key == 'page' && ! empty( $item ) ) {

				if( false !== strpos( $item, "p." ) ){
					$new_id = str_replace('p.', '', $item);
					// check if this has been imported before
					$new_meta_val = $this->_imported_post_id( $new_id );
					if ( $new_meta_val ) {
						$item = 'p.' . $new_meta_val;
					}
				}else if(is_numeric($item)){
					// check if this has been imported before
					$new_meta_val = $this->_imported_post_id( $item );
					if ( $new_meta_val ) {
						$item = $new_meta_val;
					}
				}
			}
			if ( $key == 'post_id' && ! empty( $item ) && is_numeric( $item ) ) {
				// check if this has been imported before
				$new_meta_val = $this->_imported_post_id( $item );
				if ( $new_meta_val ) {
					$item = $new_meta_val;
				}
			}
			if ( $key == 'url' && ! empty( $item ) && strstr( $item, 'ocalhost' ) ) {
				// check if this has been imported before
				$new_meta_val = $this->_imported_post_id( $item );
				if ( $new_meta_val ) {
					$item = $new_meta_val;
				}
			}
			if ( ($key == 'shortcode' || $key == 'editor') && ! empty( $item ) ) {
				// we have to fix the [contact-form-7 id=133] shortcode issue.
				$item = $this->_parse_gallery_shortcode_content($item);

			}
		}


		private function _get_json( $file ) {
			if ( is_file( __DIR__ . '/content/' . basename( $file ) ) ) {
				WP_Filesystem();
				global $wp_filesystem;
				$file_name = __DIR__ . '/content/' . basename( $file );
				if ( file_exists( $file_name ) ) {
					return json_decode( $wp_filesystem->get_contents( $file_name ), true );
				}
			}

			return array();
		}

		private function _get_sql( $file ) {
			if ( is_file( __DIR__ . '/content/' . basename( $file ) ) ) {
				WP_Filesystem();
				global $wp_filesystem;
				$file_name = __DIR__ . '/content/' . basename( $file );
				if ( file_exists( $file_name ) ) {
					return $wp_filesystem->get_contents( $file_name );
				}
			}

			return false;
		}


		public $logs = array();

		public function log( $message ) {
			$this->logs[] = $message;
		}

		public $errors = array();

		public function error( $message ) {
			$this->logs[] = 'ERROR!!!! ' . $message;
		}

		public function envato_setup_help_support() {
			?>
			<h1><?php esc_html_e('Help and Support', 'pgs-woo-api' );?></h1>
			<p><?php esc_html_e('The Application can be used on one website. To use on another site, please buy an additional license.', 'pgs-woo-api' );?></p>
			<p><?php echo sprintf(
				// Translators: %s is the theme support link.
				wp_kses( __( 'You can get the item support from <a href="%s" target="_blank" rel="noopener">Potenza Support Center</a> and that is comprised of:', 'pgs-woo-api' ), pgs_woo_api_allowed_html(array('a')) ),
				'https://potezasupport.ticksy.com/'
			);?></p>

			<div class="help-support-wrapper">
				<div class="help-support-bullets clearfix">
					<div class="includes">
						<h3><?php esc_html_e( 'Item Support comprises of:', 'pgs-woo-api' );?></h3>
						<ul>
							<li><?php esc_html_e( 'In detail explanation of the technical elements of the product', 'pgs-woo-api' );?></li>
							<li><?php esc_html_e( 'Help if there is any error or concern.', 'pgs-woo-api' );?></li>
							<li><?php esc_html_e( 'Extensive support for 3rd party plugins (bundled).', 'pgs-woo-api' );?></li>
						</ul>
					</div>
					<div class="excludes">
						<h3><?php echo wp_kses( __('Item Support <strong>DOES NOT</strong> comprise of:', 'pgs-woo-api' ), array( 'strong' => array() ) );?></h3>
						<ul>
							<li><?php esc_html_e( 'Customization services', 'pgs-woo-api' );?></li>
							<li><?php esc_html_e( 'Installation services', 'pgs-woo-api' );?></li>
							<li><?php esc_html_e( 'Assistance for non-bundled 3rd party plugins.', 'pgs-woo-api' );?></li>
						</ul>
					</div>
				</div>
			</div>

			<p><?php echo sprintf(
				 // Translators: %s is the ThemeForest Item Support Policy link.
				wp_kses( __( 'ThemeForest <a href="%s" target="_blank" rel="noopener">Item Support Policy</a> can be used to gather extra details about the item support.', 'pgs-woo-api' ), pgs_woo_api_allowed_html(array('a')) ),
				'http://themeforest.net/page/item_support_policy'
			);?></p>
			<p class="envato-setup-actions step">
				<a href="<?php echo esc_url( $this->get_next_step_link() ); ?>" class="button button-large button-primary button-active"><?php esc_html_e( 'Continue', 'pgs-woo-api' ); ?></a>
				<?php wp_nonce_field( 'envato-setup' ); ?>
			</p>
			<?php
		}

		/**
		 * Final step
		 */
		public function envato_setup_ready() {

			update_option( 'pgs_woo_api_envato_setup_complete', time() );
			update_option( $this->pgs_plugin_name.'_setup_wizard_displayed', true );
			?>
			<div class="envato-setup-done dashicons dashicons-admin-site"><div class="envato-setup-done-check dashicons dashicons-yes">&nbsp;</div></div>

			<h1><?php esc_html_e( 'Your Settings are Ready!', 'pgs-woo-api' ); ?></h1>

			<p><?php esc_html_e( 'Praises to you, your settings are ready now. The required plugins have been turned on to enhance the functionality of your site. You can modify the content and make required changes, if important by logging into the Wordpress Dashboard.', 'pgs-woo-api' ); ?></p>
			<p><?php echo sprintf(
				// Translators: %s is the Themeforest downloads link.
				wp_kses( __( 'Please encourage us by <a href="%s" target="_blank" rel="noopener">dropping 5-star</a>.', 'pgs-woo-api' ), pgs_woo_api_allowed_html(array('a')) ),
				'https://themeforest.net/downloads'
			);?></p>
			<br>
			<br>
			<div class="envato-setup-final-contents">
				<div class="envato-setup-final-content envato-setup-final-content-first">
					<div class="envato-setup-final-content-inner">
						<div class="envato-setup-final-header"><h2><?php esc_html_e( 'Next Steps', 'pgs-woo-api' ); ?></h2></div>
						<ul>
							<li class="envato-setup-final-step-button"><a class="button button-primary button-large" href="http://themeforest.net/user/potenzaglobalsolutions/follow" target="_blank" rel="noopener"><?php esc_html_e( 'Follow PotenzaGlobalSolutions on ThemeForest', 'pgs-woo-api' ); ?></a></li>
							<li class="envato-setup-final-step-button"><a class="button button-large" href="<?php echo esc_url( admin_url('admin.php?page=pgs-woo-api-token-settings') ); ?>"><?php esc_html_e( 'Please configure your OAuth Credentials', 'pgs-woo-api' ); ?><br /><?php esc_html_e( 'if already set ignore and move to build your App', 'pgs-woo-api' ); ?></a></li>
						</ul>
					</div>
				</div>
				<div class="envato-setup-final-content envato-setup-final-content-last">
					<div class="envato-setup-final-content-inner">
						<div class="envato-setup-final-header"><h2><?php esc_html_e( 'More Resources', 'pgs-woo-api' ); ?></h2></div>
						<div class="more-resources">
							<div class="more-resource documentation"><a href="http://docs.potenzaglobalsolutions.com/ciya-shop-mobile-apps/" target="_blank" rel="noopener"><?php esc_html_e( 'Read the Documentation', 'pgs-woo-api' ); ?></a></div>
							<div class="more-resource howto"><a href="https://wordpress.org/support/" target="_blank" rel="noopener"><?php esc_html_e( 'Learn how to use WordPress', 'pgs-woo-api' ); ?></a></div>
							<div class="more-resource rating"><a href="https://themeforest.net/downloads" target="_blank" rel="noopener"><?php esc_html_e( 'Leave an Item Rating', 'pgs-woo-api' ); ?></a></div>
							<div class="more-resource support"><a href="https://potezasupport.ticksy.com/" target="_blank" rel="noopener"><?php esc_html_e( 'Get Help and Support', 'pgs-woo-api' ); ?></a></div>
						</div>
					</div>
				</div>
			</div>
			<?php
		}



		/**
		 * @param $array1
		 * @param $array2
		 *
		 * @return mixed
		 *
		 *
		 * @since    1.1.4
		 */
		private function _array_merge_recursive_distinct( $array1, $array2 ) {
			$merged = $array1;
			foreach ( $array2 as $key => &$value ) {
				if ( is_array( $value ) && isset( $merged [ $key ] ) && is_array( $merged [ $key ] ) ) {
					$merged [ $key ] = $this->_array_merge_recursive_distinct( $merged [ $key ], $value );
				} else {
					$merged [ $key ] = $value;
				}
			}

			return $merged;
		}

		/**
		 * Helper function
		 * Take a path and return it clean
		 *
		 * @param string $path
		 *
		 * @since    1.1.2
		 */
		public static function cleanFilePath( $path ) {
			$path = str_replace( '', '', str_replace( array( '\\', '\\\\', '//' ), '/', $path ) );
			if ( $path[ strlen( $path ) - 1 ] === '/' ) {
				$path = rtrim( $path, '/' );
			}

			return $path;
		}

		public function is_submenu_page() {
			return ( $this->parent_slug == '' ) ? false : true;
		}

		/*public function get_stored_code() {
			$code = false;
			return $code;
		}*/

		public function domain() {
			$domain = get_option('siteurl'); //or home
			$domain = str_replace('http://', '', $domain);
			$domain = str_replace('https://', '', $domain);
			$domain = str_replace('www', '', $domain); //add the . after the www if you don't want it
			return urlencode($domain);
		}

		public static function after_theme_switch(){
			return ''; // wizard, other
		}

	}

}// if !class_exists

/**
 * Loads the main instance of Envato_PGS_API_Plugin_Setup_Wizard to have
 * ability extend class functionality
 *
 * @since 1.1.1
 * @return object Envato_PGS_API_Plugin_Setup_Wizard
 */
add_action( 'after_setup_theme', 'envato_pgs_woo_api_setup_wizard', 10 );
if ( ! function_exists( 'envato_pgs_woo_api_setup_wizard' ) ) :
	function envato_pgs_woo_api_setup_wizard() {
        Envato_PGS_API_Plugin_Setup_Wizard::get_instance();
	}
endif;

// Display admin notice if required plugins are not active
function pgs_woo_api_plugin_setup_wizard_notice() {

	$current_plugin = pgs_woo_api_get_plugin_data();
    $plugin_slug = str_replace('-', '_', sanitize_title($current_plugin['Name']));

	if ( get_option( $plugin_slug.'_setup_wizard_displayed', false ) ) {
		return;
	}
	?>
	<div class="notice notice-error">
		<p><strong><?php echo sprintf(
			// Translators: %s is the theme name.
			esc_html__( "Welcome to %s", 'pgs-woo-api' ),
			'PGS Woo API'//$current_theme->get( 'Name' )
		);?></strong></p>
		<p><?php
			esc_html_e( 'You\'re almost there. PGS Woo API contains many useful features and functionalities. For this, some settings are to be done, to enable all features and functionalities. And, PGS Woo API Setup Wizard might be of a great help to you for same.', 'pgs-woo-api' );?></p>
		<p><a href="<?php echo esc_url( admin_url( 'admin.php?page='.$plugin_slug.'-setup' ) ); ?>" class="button-primary"><?php esc_html_e( 'Run the Setup Wizard', 'pgs-woo-api' ); ?></a> <a class="button-secondary skip" href="<?php echo esc_url( wp_nonce_url( add_query_arg( 'pgs-woo-api-setup-hide-notice', '1' ), 'pgs_woo_api_setup_hide_notice_nonce', '_cssetup_notice_nonce' ) ); ?>"><?php esc_html_e( 'Skip setup', 'pgs-woo-api' ); ?></a></p>
	</div>
	<?php
}
add_action( 'admin_notices', 'pgs_woo_api_plugin_setup_wizard_notice' );


function pgs_woo_api_hide_setup_wizard_notice() {

	$current_plugin = pgs_woo_api_get_plugin_data();
    $plugin_slug = str_replace('-', '_', sanitize_title($current_plugin['Name']));

	if ( isset( $_GET['pgs-woo-api-setup-hide-notice'] ) && isset( $_GET['_cssetup_notice_nonce'] ) ) { // WPCS: input var ok, CSRF ok.
		if ( ! wp_verify_nonce( sanitize_key( $_GET['_cssetup_notice_nonce'] ), 'pgs_woo_api_setup_hide_notice_nonce' ) ) { // WPCS: input var ok, CSRF ok.
			wp_die( esc_html__( 'Action failed. Please refresh the page and retry.', 'pgs-woo-api' ) );
		}

		if ( ! current_user_can( 'manage_options' ) ) {
			wp_die( esc_html__( 'You don&#8217;t have permission to do this.', 'pgs-woo-api' ) );
		}

		$hide_notice = sanitize_text_field( $_GET['pgs-woo-api-setup-hide-notice'] );
		if( $hide_notice != '' && $hide_notice == 1 ){
			update_option( $plugin_slug.'_setup_wizard_displayed', true );
		}
		$url = remove_query_arg( array('pgs-woo-api-setup-hide-notice', '_cssetup_notice_nonce') );
		wp_safe_redirect( $url );
		exit;
	}
}
add_action( 'admin_init', 'pgs_woo_api_hide_setup_wizard_notice' );
