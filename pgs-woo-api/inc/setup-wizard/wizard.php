<?php
global $pgs_woo_api_globals;
$pgs_woo_api_globals = pgs_woo_api_get_plugin_data();
require_once( PGS_API_PATH . 'inc/setup-wizard/envato_setup/envato_setup_init.php' );
require_once( PGS_API_PATH . 'inc/setup-wizard/envato_setup/envato_setup.php' );

//add_filter( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_name', 'pgs_api_plugin_envato_setup_wizard_pgs_woo_api_name' );
function pgs_api_plugin_envato_setup_wizard_pgs_woo_api_name($pgs_plugin_name){
	global $pgs_woo_api_globals;
	$pgs_plugin_name = $pgs_woo_api_globals['pgs_plugin_name'];
	return $pgs_plugin_name;
}

add_filter( 'pgs_woo_api_envato_setup_logo_image', 'app_pgs_woo_api_envato_setup_logo_image' );
function app_pgs_woo_api_envato_setup_logo_image( $image_url ){
	$logo_path = PGS_API_PATH.'img/logo.png';
	$logo_url = PGS_API_URL.'img/logo.png';
	if( file_exists($logo_path) ){
		$image_url = $logo_url;
	}
	return $image_url;
}

add_filter( 'pgs_woo_api_plugin_setup_wizard_steps', 'pgs_woo_api_plugin_setup_wizard_steps_extend' );
function pgs_woo_api_plugin_setup_wizard_steps_extend( $steps ){
	if( isset($steps['design']) ) unset($steps['design']);
	return $steps;
}

// Please don't forgot to change filters tag.
// It must start from your theme's name.
add_filter( $pgs_woo_api_globals['pgs_plugin_name'] . '_theme_setup_wizard_username', 'pgs_woo_api_set_theme_setup_wizard_username', 10 );
if( ! function_exists('pgs_woo_api_set_theme_setup_wizard_username') ){
    function pgs_woo_api_set_theme_setup_wizard_username($username){
        return 'potenzaglobalsolutions';
    }
}

add_filter( $pgs_woo_api_globals['pgs_plugin_name'] . '_theme_setup_wizard_oauth_script', 'pgs_woo_api_set_theme_setup_wizard_oauth_script', 10 );
if( ! function_exists('pgs_woo_api_set_theme_setup_wizard_oauth_script') ){
    function pgs_woo_api_set_theme_setup_wizard_oauth_script($oauth_url){
        return 'http://themes.potenzaglobalsolutions.com/api/envato/auth.php';
    }
}

add_filter( 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_styles', 'envato_pgs_api_plugin_setup_wizard_pgs_woo_api_styles', 10 );
if( ! function_exists('envato_pgs_api_plugin_setup_wizard_pgs_woo_api_styles') ){
    function envato_pgs_api_plugin_setup_wizard_pgs_woo_api_styles( $styles ){

		$styles = array(
			'style_1' => 'Style 1',
			'style_2' => 'Style 2',
			'style_3' => 'Style 3',
		);

		$styles = pgs_woo_api_sample_data_items();

		return $styles;
    }
}

add_filter( $pgs_woo_api_globals['pgs_plugin_name'] . '_theme_setup_wizard_default_theme_style', 'pgs_woo_api_set_envato_setup_default_theme_style' );
function pgs_woo_api_set_envato_setup_default_theme_style($style){

	$style = 'default';

	return $style;
}

function pgs_woo_api_plugin_name_scripts() {
	/* Add Your Custom CSS and JS */
}
add_action( 'admin_init', 'pgs_woo_api_plugin_name_scripts', 20 );

add_action( 'admin_head', 'pgs_woo_api_plugin_setup_wizard_set_assets', 0 );
function pgs_woo_api_plugin_setup_wizard_set_assets(){
	wp_print_scripts( 'ciyashop-theme-setup' );
}

add_filter( 'envato_setup_wizard_footer_copyright', 'pgs_woo_api_plugin_envato_setup_wizard_footer_copyright', 10, 2 );
function pgs_woo_api_plugin_envato_setup_wizard_footer_copyright( $copyright, $pgs_plugin_data ){

	/* translators: %s: Postenza Global Solutions (Name of Theme Developer) */
	$copyright = sprintf( esc_html__( '&copy; Created by %s', 'pgs-woo-api' ),
		sprintf( '<a href="%s" target="_blank">%s</a>',
			'http://www.potenzaglobalsolutions.com/',
			esc_html__( 'Potenza Global Solutions', 'pgs-woo-api' )
		)
	);

	return $copyright;
}

add_filter( 'envato_pgs_api_plugin_setup_wizard_themeforest_profile_url', 'pgs_api_plugin_envato_setup_wizard_themeforest_profile_url' );
function pgs_api_plugin_envato_setup_wizard_themeforest_profile_url( $url ){
	$url = '';
	return $url;
}
