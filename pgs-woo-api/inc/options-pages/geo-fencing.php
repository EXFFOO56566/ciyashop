<?php
/*
 * Register Post Type for Geofencing
 */
add_action( 'init', 'pgs_woo_api_geo_fencing_cpt' , 1 );
function pgs_woo_api_geo_fencing_cpt() {
	$labels = array(
		'name'               => esc_html__( 'Geofencing', 'pgs-woo-api' ),
		'singular_name'      => esc_html__( 'Geofencing', 'pgs-woo-api' ),
		'menu_name'          => esc_html__( 'Geofencing', 'pgs-woo-api' ),
		'name_admin_bar'     => esc_html__( 'Geofencing', 'pgs-woo-api' ),
		'add_new'            => esc_html__( 'Add New', 'pgs-woo-api' ),
		'add_new_item'       => esc_html__( 'Add New Geofencing', 'pgs-woo-api' ),
		'new_item'           => esc_html__( 'New Geofencing', 'pgs-woo-api' ),
		'edit_item'          => esc_html__( 'Edit Geofencing', 'pgs-woo-api' ),
		'view_item'          => esc_html__( 'View Geofencing', 'pgs-woo-api' ),
		'all_items'          => esc_html__( 'All Geofencing', 'pgs-woo-api' ),
		'search_items'       => esc_html__( 'Search Geofencing', 'pgs-woo-api' ),
		'parent_item_colon'  => esc_html__( 'Parent Geofencing:', 'pgs-woo-api' ),
		'not_found'          => esc_html__( 'No geofencing found.', 'pgs-woo-api' ),
		'not_found_in_trash' => esc_html__( 'No geofencing found in Trash.', 'pgs-woo-api' )
	);

	$args = array(
		'labels'             => $labels,
		'description'        => esc_html__( 'Description.', 'pgs-woo-api' ),
		'public'             => false,
		'publicly_queryable' => false,
		'show_ui'            => true,
		'show_in_menu'       => true,
		'show_in_menu'       => 'admin.php?page=pgs-woo-api-settings',
		'query_var'          => true,
		'rewrite'            => array( 'slug' => 'geofencing' ),
		'capability_type'    => 'post',
		'exclude_from_search' => true,
		'has_archive'        => false,
		'hierarchical'       => false,
		'menu_position'      => null,
		'supports'           => array( 'title', 'author', 'editor' ),
		'menu_icon' 		 => 'dashicons-location',
	);
	register_post_type( 'geofencing', $args );
}
/**
 * Setting page for auth and other options settings
 */
add_action('admin_menu', 'pgs_woo_api_geo_fencing_option_page_submenu');
function pgs_woo_api_geo_fencing_option_page_submenu(){
	// Create submenu with href to view custom_plugin_post_type
	add_submenu_page( "pgs-woo-api-settings", esc_html('Geofencing','pgs-woo-api'), esc_html('Geofencing','pgs-woo-api'), 'manage_options', 'edit.php?post_type=geofencing');
}


/**
 * Register meta box(es).
 */
function pgs_woo_api_geo_meta_boxes() {
    add_meta_box( 'geo_fencing', esc_html__( 'Geofencing', 'pgs-woo-api' ), 'pgs_woo_api_geofencing_callback', 'geofencing' );
}
add_action( 'add_meta_boxes', 'pgs_woo_api_geo_meta_boxes' );

/**
 * Meta box display callback.
 */
function pgs_woo_api_geofencing_callback( $post ) {

    $radius = 0;
    $lat    = 43.653226;//get_post_meta($post->ID,'lat',true);
    $lng    = -79.3831843;//get_post_meta($post->ID,'lng',true);
    $zoom    = 10;//get_post_meta($post->ID,'zoom',true);

    $radius = get_post_meta($post->ID,'radius',true);
    $lat    = get_post_meta($post->ID,'lat',true);
    $lng    = get_post_meta($post->ID,'lng',true);
    $zoom   = get_post_meta($post->ID,'zoom',true);
    $geo_location = get_post_meta($post->ID,'geo_location',true);
    wp_nonce_field(basename(__FILE__), "meta-box-nonce");

    $geoObj = array(
		'radius' => (double)$radius,
        'lat' => (double)$lat,
        'lng' => (double)$lng,
        'zoom' => $zoom

	);
	wp_localize_script( 'pgs-woo-api-google-maps-apis', 'pgs_woo_api_map', $geoObj );
    wp_enqueue_style( 'pgs-woo-api-geofance-css' );
    wp_enqueue_script( 'pgs-woo-api-geofance');
    wp_enqueue_script( 'pgs-woo-api-google-maps-apis');
    ?>
    <input type="text" id="pac-input" name="geo_location" value="<?php echo esc_attr($geo_location)?>" class="widefat"/>
    <input type="hidden" id="radius" name="radius" value="<?php echo esc_attr($radius)?>" class="widefat" />
    <input type="hidden" id="lat" name="lat" value="<?php echo esc_attr($lat)?>" class="widefat" />
    <input type="hidden" id="lng" name="lng" value="<?php echo esc_attr($lng)?>" class="widefat" />
    <input type="hidden" id="zoom" name="zoom" value="<?php echo esc_attr($zoom)?>"/>
    <div id="map" class="geo-map"></div>
    <?php
}

/**
 * Save meta box content.
 */
function pgs_woo_api_geofenc_save_meta_box( $post_id ) {

    if (!isset($_POST["meta-box-nonce"]) || !wp_verify_nonce($_POST["meta-box-nonce"], basename(__FILE__)))
        return $post_id;

    if(!current_user_can("edit_post", $post_id))
        return $post_id;

    if(defined("DOING_AUTOSAVE") && DOING_AUTOSAVE)
        return $post_id;

    $radius = 0;$lat = 0;$lng = 0;$zoom=12;
    if(isset($_POST["radius"])){
        $radius = $_POST["radius"];
    }
    update_post_meta($post_id, "radius", $radius);

    if(isset($_POST["lat"])){
        $lat = $_POST["lat"];
    }
    update_post_meta($post_id, "lat", $lat);

    if(isset($_POST["lng"])){
        $lng = $_POST["lng"];
    }
    update_post_meta($post_id, "lng", $lng);

    if(isset($_POST["zoom"])){
        $zoom = $_POST["zoom"];
    }
    update_post_meta($post_id, "zoom", $zoom);

    if(isset($_POST["geo_location"])){
        $geo_location = $_POST["geo_location"];
    }
    update_post_meta($post_id, "geo_location", $geo_location);

    global $wpdb;
    $table_name = $wpdb->prefix . "pgs_woo_api_geo_fencing";
    $results = $wpdb->get_results( "SELECT * FROM $table_name WHERE post_id = $post_id", OBJECT );

    $data = array(
        'post_id' => $post_id,
        'radius' => $radius,
        'lat' => $lat,
        'lng' => $lng,
    );
    if(!empty($results)){
        $wpdb->update($table_name, $data, array('post_id' => $post_id));
    } else {
        $wpdb->insert($table_name, $data);
    }
}
add_action( 'save_post', 'pgs_woo_api_geofenc_save_meta_box' );

add_action( 'before_delete_post', 'pgs_woo_api_geofenc_before_delete_entry' );
function pgs_woo_api_geofenc_before_delete_entry( $postid ){
    global $post_type,$post;
    if ( $post_type != 'geofencing' ) return;
    global $wpdb;
    $geo_fencing = $wpdb->prefix . "pgs_woo_api_geo_fencing";
    $wpdb->delete( $geo_fencing, array( 'post_id' => $post->ID ) );
}?>