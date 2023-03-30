<?php
/**
 * This file represents an example of the code that themes would use to register
 * the required plugins.
 *
 * It is expected that theme authors would copy and paste this code into their
 * functions.php file, and amend to suit.
 *
 * @see http://tgmpluginactivation.com/configuration/ for detailed documentation.
 *
 * @package    TGM-Plugin-Activation
 * @subpackage Example
 * @version    2.6.1 for plugin pgs-woo-api
 * @author     Thomas Griffin, Gary Jones, Juliette Reinders Folmer
 * @copyright  Copyright (c) 2011, Thomas Griffin
 * @license    http://opensource.org/licenses/gpl-2.0.php GPL v2 or later
 * @link       https://github.com/TGMPA/TGM-Plugin-Activation
 */

/**
 * Include the TGM_Plugin_Activation class.
 *
 * Depending on your implementation, you may want to change the include call:
 *
 * Parent Theme:
 * require_once get_template_directory() . '/path/to/class-tgm-plugin-activation.php';
 *
 * Child Theme:
 * require_once get_stylesheet_directory() . '/path/to/class-tgm-plugin-activation.php';
 *
 * Plugin:
 * require_once dirname( __FILE__ ) . '/path/to/class-tgm-plugin-activation.php';
 */
require_once PGS_API_PATH.'/inc/tgm-plugin-activation/core/class-tgm-plugin-activation.php';


add_action( 'tgmpa_register', 'pgs_woo_api_register_required_plugins' );

/**
 * Register the required plugins for this theme.
 *
 * In this example, we register five plugins:
 * - one included with the TGMPA library
 * - two from an external source, one from an arbitrary source, one from a GitHub repository
 * - two from the .org repo, where one demonstrates the use of the `is_callable` argument
 *
 * The variables passed to the `tgmpa()` function should be:
 * - an array of plugin arrays;
 * - optionally a configuration array.
 * If you are not changing anything in the configuration array, you can remove the array and remove the
 * variable from the function call: `tgmpa( $plugins );`.
 * In that case, the TGMPA default settings will be used.
 *
 * This function is hooked into `tgmpa_register`, which is fired on the WP `init` action on priority 10.
 */
function pgs_woo_api_register_required_plugins() {
	/*
	 * Array of plugin arrays. Required keys are name and slug.
	 * If the source is NOT from the .org repo, then source is also required.
	 */
	$plugins = pgs_woo_api_tgmpa_plugin_list();

	/*
	 * Array of configuration settings. Amend each line as needed.
	 *
	 * TGMPA will start providing localized text strings soon. If you already have translations of our standard
	 * strings available, please help us make TGMPA even better by giving us access to these translations or by
	 * sending in a pull-request with .po file(s) with the translations.
	 *
	 * Only uncomment the strings in the config array if you want to customize the strings.
	 */
	$config = array(
		'id'           => 'pgs-woo-api',                 // Unique ID for hashing notices for multiple instances of TGMPA.
		'default_path' => '',                      // Default absolute path to bundled plugins.
		'menu'         => 'theme-plugins', // Menu slug.
		'parent_slug'  => 'themes.php',            // Parent menu slug.
		'capability'   => 'edit_theme_options',    // Capability needed to view plugin install page, should be a capability associated with the parent menu used.
		'has_notices'  => true,                    // Show admin notices or not.
		'dismissable'  => true,                    // If false, a user cannot dismiss the nag message.
		'dismiss_msg'  => '',                      // If 'dismissable' is false, this message will be output at top of nag.
		'is_automatic' => false,                   // Automatically activate plugins after installation or not.
		'message'      => '',                      // Message to output right before the plugins table.
	);

	pgs_woo_api_tgmpa( $plugins, $config );
}





if ( ! function_exists( 'pgs_woo_api_tgmpa' ) ) {
	/**
	 * Helper function to register a collection of required plugins.
	 *
	 * @since 2.0.0
	 * @api
	 *
	 * @param array $plugins An array of plugin arrays.
	 * @param array $config  Optional. An array of configuration values.
	 */
	function pgs_woo_api_tgmpa( $plugins, $config = array() ) {
		$instance = call_user_func( array( get_class( $GLOBALS['pgs_api_tgmpa'] ), 'get_instance' ) );

		foreach ( $plugins as $plugin ) {
			call_user_func( array( $instance, 'register' ), $plugin );
		}

		if ( ! empty( $config ) && is_array( $config ) ) {
			// Send out notices for deprecated arguments passed.
			if ( isset( $config['notices'] ) ) {
				_deprecated_argument( __FUNCTION__, '2.2.0', 'The `notices` config parameter was renamed to `has_notices` in TGMPA 2.2.0. Please adjust your configuration.' );
				if ( ! isset( $config['has_notices'] ) ) {
					$config['has_notices'] = $config['notices'];
				}
			}

			if ( isset( $config['parent_menu_slug'] ) ) {
				_deprecated_argument( __FUNCTION__, '2.4.0', 'The `parent_menu_slug` config parameter was removed in TGMPA 2.4.0. In TGMPA 2.5.0 an alternative was (re-)introduced. Please adjust your configuration. For more information visit the website: http://tgmpluginactivation.com/configuration/#h-configuration-options.' );
			}
			if ( isset( $config['parent_url_slug'] ) ) {
				_deprecated_argument( __FUNCTION__, '2.4.0', 'The `parent_url_slug` config parameter was removed in TGMPA 2.4.0. In TGMPA 2.5.0 an alternative was (re-)introduced. Please adjust your configuration. For more information visit the website: http://tgmpluginactivation.com/configuration/#h-configuration-options.' );
			}

			call_user_func( array( $instance, 'config' ), $config );
		}
	}
}



/*
 * Array of plugin arrays. Required keys are name and slug.
 * If the source is NOT from the .org repo, then source is also required.
 */
function pgs_woo_api_tgmpa_plugin_list(){

	$tgmpa_plugin_list =  array(
		// This is an example of how to include a plugin bundled with a theme.
		array(
			'name'              => 'WooCommerce', // The plugin name.
			'slug'              => 'woocommerce', // The plugin slug (typically the folder name).
			'required'          => true,
			'details_url'       => 'https://wordpress.org/plugins/woocommerce/',
			'checked_in_wizard' => true,
		),
        array(
			'name'               => 'WordPress REST API(OAuth 1.0a Server)', // The plugin name.
			'slug'               => 'rest-api-oauth1', // The plugin slug (typically the folder name).
			'source'             => PGS_API_PATH . '/inc/tgm-plugin-activation/core/plugins/rest-api-oauth1.0.3.0.zip', // The plugin source.
			'required'           => true, // If false, the plugin is only 'recommended' instead of required.
			'version'            => '', // E.g. 1.0.0. If set, the active plugin must be this version or higher. If the plugin version is higher than the plugin version installed, the user will be notified to update the plugin.
			'force_activation'   => false, // If true, plugin is activated upon theme activation and cannot be deactivated until theme switch.
			'force_deactivation' => false, // If true, plugin is deactivated upon theme switch, useful for theme-specific plugins.
			'external_url'       => '', // If set, overrides default API URL and points to an external URL.
			'is_callable'        => '', // If set, this callable will be be checked for availability to determine if a plugin is active.
            'checked_in_wizard'  => true
        )
	);
    return apply_filters( 'pgs_woo_api_tgmpa_plugin_list', $tgmpa_plugin_list );
}



/*
 * pgs_woo_api_tgmpa_plugins_data()
 * Returns plugin activation list
 */
function pgs_woo_api_tgmpa_plugins_data(){
	$plugins = pgs_woo_api_tgmpa_plugin_list();

	$tgmpax = call_user_func( array( get_class( $GLOBALS['pgs_api_tgmpa'] ), 'get_instance' ) );
	foreach ( $plugins as $plugin ) {
		call_user_func( array( $tgmpax, 'register' ), $plugin );
	}
	$pluginx = $tgmpax->plugins;

	$pluginy = array(
		'all'      => array(), // Meaning: all plugins which still have open actions.
		'install'  => array(),
		'update'   => array(),
		'activate' => array(),
	);

	foreach ( $tgmpax->plugins as $slug => $plugin ) {
		if ( $tgmpax->is_plugin_active( $slug ) && false === $tgmpax->does_plugin_have_update( $slug ) ) {
			// No need to display plugins if they are installed, up-to-date and active.
			continue;
		} else {
			$pluginy['all'][ $slug ] = $plugin;

			if ( ! $tgmpax->is_plugin_installed( $slug ) ) {
				$pluginy['install'][ $slug ] = $plugin;
			} else {
				if ( false !== $tgmpax->does_plugin_have_update( $slug ) ) {
					$pluginy['update'][ $slug ] = $plugin;
				}

				if ( $tgmpax->can_plugin_activate( $slug ) ) {
					$pluginy['activate'][ $slug ] = $plugin;
				}
			}
		}
	}
	return $pluginy;
}

function pgs_woo_api_tgmpa_plugin_path( $plugin_name = '' ){

	$purchase_token= pgs_woo_api_is_activated();

	// bail early if no plugin name provided
	if( empty($plugin_name) ) return '';

	return add_query_arg( array(
		'plugin_name' => $plugin_name,
		'token'       => $purchase_token,
		'site_url'    => get_site_url(),
		'product_key' => PGS_PRODUCT_KEY,
	), trailingslashit(PGS_ENVATO_API) . 'install-plugin' );
}
