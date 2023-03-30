<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}
class PGS_WOO_API_WishlistController extends PGS_WOO_API_Controller{
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
	protected $rest_base = 'wishlist';

	public function __construct() {
		$this->register_routes();
	}
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route'));
	}


	public function pgs_woo_api_register_route() {

        register_rest_route( $this->namespace, $this->rest_base, array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this,'pgs_woo_api_wishlist'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'add_wishlist', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this,'pgs_woo_api_add_wishlist'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

        register_rest_route( $this->namespace, 'remove_wishlist', array(
    		'methods' => WP_REST_Server::CREATABLE,//'POST',
    		'callback' => array($this,'pgs_woo_api_remove_wishlist'),
            'permission_callback' => array($this, 'pgs_woo_api_permission_callback'),
    	) );

    }

    /**
     * Sycrononce with current user list and add or update in database
     * Get all list data
     */
    public function pgs_woo_api_wishlist(){

        $error = array( "status" => "error" );
        $input = file_get_contents("php://input");
        $obj = json_decode($input,true);

        if(!isset($obj['user_id'])){
            $returnArr['error'] = true;
        	$returnArr['message'] = esc_html__('Sorry, that is not valid input. You missed user_id parameters','pgs-woo-api');
        	return $returnArr;

        }

        $sync_list = $obj['sync_list'];
        if(isset($sync_list)){
            if(!empty($sync_list)){
                foreach($sync_list as $list){

                    $prod_id = ( isset( $list['product_id'] ) && is_numeric( $list['product_id'] ) ) ? $list['product_id'] : false;
                    $wishlist_id = ( isset( $list['wishlist_id'] ) && strcmp( $list['wishlist_id'], 0 ) != 0 ) ? $list['wishlist_id'] : false;
                    $quantity = ( isset( $list['quantity'] ) ) ? ( int ) $list['quantity'] : 1;
                    $wishlist_name = ( ! empty( $list['wishlist_name'] ) ) ? $list['wishlist_name'] : '';
                    $user_id = ( ! empty( $list['user_id'] ) ) ? $list['user_id'] : false;
                    $this->pgs_add_update_to_list($prod_id,$wishlist_id,$quantity,$wishlist_name,$user_id);
                }
            }
        } else {
            $error['error'] = esc_html__("No data for sync.","pgs-woo-api");
            return $error;

        }
        $wishlists = $this->pgs_woo_api_all_wishlists( array(
            'user_id' => $obj['user_id'],
            'is_default' => 1
        ) );
        $res = array();
        $res['status'] = 'success';
        $res['sync_list'] = $wishlists;
        return $res;
    }

    /**
     *
     * Get all list data form user
     */
    private function pgs_woo_api_all_wishlists($data){
        global $wpdb;

        $yith_wcwl_wishlists = YITH_WCWL_WISHLISTS_TABLE;
        $yith_wcwl_items = YITH_WCWL_ITEMS_TABLE;
        $user_id = $data['user_id'];

        $hidden_products = $this->pgs_get_hidden_products();
        $sql = "SELECT * FROM $yith_wcwl_items WHERE  user_id = $user_id";
        if(!empty($hidden_products)){
            $sql .= $hidden_products ? " AND prod_id NOT IN ( " . implode( ', ', $hidden_products ) . " )" : "";
        }

        $lists = $wpdb->get_results( $sql, ARRAY_A );
        if(!empty($lists)){
            return $lists;
        } else {
            $error['error'] = esc_html__("No record found.",'pgs-woo-api');
            return $error;

        }
    }

    private function pgs_get_hidden_products(){
        global $wpdb;
        $hidden_products = array();

        if( version_compare( WC()->version, '3.0.0', '<' ) ){
            $query = "SELECT p.ID
                      FROM {$wpdb->posts} AS p
                      LEFT JOIN {$wpdb->postmeta} AS pm ON p.ID = pm.post_id
                      WHERE meta_key = %s AND meta_value <> %s";
            $query_args = array(
                '_visibility',
                'visible'
            );
        }
        else{
            $product_visibility_term_ids = wc_get_product_visibility_term_ids();
            $query = "SELECT tr.object_id
                      FROM {$wpdb->term_relationships} AS tr
                      LEFT JOIN {$wpdb->term_taxonomy} AS tt USING( term_taxonomy_id )
                      WHERE tt.taxonomy = %s AND tr.term_taxonomy_id = %d";
            $query_args = array(
                'product_visibility',
                $product_visibility_term_ids['exclude-from-catalog']
            );
        }

        $hidden_products = $wpdb->get_col( $wpdb->prepare( $query, $query_args ) );
        return $hidden_products;
    }


    /**
     *
     * Add or Update to wishlist on bulk action
     */
    private function pgs_add_update_to_list($prod_id,$wishlist_id,$quantity,$wishlist_name,$user_id){

        global $wpdb;
        $error = array( "status" => "error" );
        $result = false;
        $last_operation_token = false;
        do_action( 'yith_wcwl_adding_to_wishlist', $prod_id, $wishlist_id, $user_id );

        // filtering params
        $prod_id = apply_filters( 'yith_wcwl_adding_to_wishlist_prod_id', $prod_id );
        $wishlist_id = apply_filters( 'yith_wcwl_adding_to_wishlist_wishlist_id', $wishlist_id );
        $quantity = apply_filters( 'yith_wcwl_adding_to_wishlist_quantity', $quantity );
        $user_id = apply_filters( 'yith_wcwl_adding_to_wishlist_user_id', $user_id );
        $wishlist_name = apply_filters( 'yith_wcwl_adding_to_wishlist_wishlist_name', $wishlist_name );

        $error = array( "status" => "error" );
        if ( $prod_id == false ) {
            $error['message'] = __( 'An error occurred while adding products to the wishlist.', 'pgs-woo-api' );
            return $error;

        }

        if( $this->pgs_woo_api_is_product_in_wishlist( $prod_id, $wishlist_id,$user_id ) ) {
            $update_args = array(
                'prod_id' => $prod_id,
                'user_id' => $user_id,
                'quantity' => $quantity,
                'dateadded' => date( 'Y-m-d H:i:s' )
            );

            $is_product = $this->pgs_is_product_exists($prod_id);
            if($is_product){
                $yith_wcwl_items = YITH_WCWL_ITEMS_TABLE;
                $result = $wpdb->update( $yith_wcwl_items, $update_args, array('prod_id' => $prod_id,'user_id' => $user_id) );
            }
        } else {

            if( $user_id != false ) {

                $insert_args = array(
                    'prod_id' => $prod_id,
                    'user_id' => $user_id,
                    'quantity' => $quantity,
                    'dateadded' => date( 'Y-m-d H:i:s' )
                );

                if( ! empty( $wishlist_id ) && strcmp( $wishlist_id, 'new' ) != 0 ){
                    $insert_args[ 'wishlist_id' ] = $wishlist_id;

                    $wishlist = $this->pgs_woo_api_get_wishlist_detail( $insert_args[ 'wishlist_id' ] );
                    $last_operation_token = $wishlist['wishlist_token'];
                }
                elseif( strcmp( $wishlist_id, 'new' ) == 0 ){
                    $response = pgs_woo_add_wishlist($user_id);

                    if( $response == "error" ){
                        return "error";
                    }
                    else{
                        $insert_args[ 'wishlist_id' ] = $response;

                        $wishlist = $this->pgs_woo_api_get_wishlist_detail( $insert_args[ 'wishlist_id' ] );
                        $last_operation_token = $wishlist['wishlist_token'];
                    }
                }
                elseif( empty( $wishlist_id ) ){
                    $wishlist_id = $this->pgs_woo_api_generate_default_wishlist( $user_id );
                    $insert_args[ 'wishlist_id' ] = $wishlist_id;

                    if( $this->pgs_woo_api_is_product_in_wishlist( $prod_id, $wishlist_id, $user_id ) ){

                        $error['message'] = "exists";
                        return $error;

                    }
                }

                $is_product = $this->pgs_is_product_exists($prod_id);

                if($is_product){
                    $yith_wcwl_items = YITH_WCWL_ITEMS_TABLE;
                    $result = $wpdb->insert( $yith_wcwl_items, $insert_args );
                }

                if( $result ){
                    if( $last_operation_token ) {
                        delete_transient( 'yith_wcwl_wishlist_count_' . $last_operation_token );
                    }

                    if( $user_id ) {
                        delete_transient( 'yith_wcwl_user_default_count_' . $user_id );
                        delete_transient( 'yith_wcwl_user_total_count_' . $user_id );
                    }
                }
            }
        }


        if( $result ) {
            do_action( 'yith_wcwl_added_to_wishlist', $prod_id, $wishlist_id, $user_id );
            $res['status'] = "success";
            $res['message'] = esc_html__("Product added!","pgs-woo-api");
            return $res;
        }
        else {
            $error['message'] = esc_html__('An error occurred while adding products to wishlist.', 'pgs-woo-api');
            return $error;

        }
    }

    private function pgs_woo_api_is_product_in_wishlist( $product_id, $wishlist_id = false,$user_id ) {
        global $wpdb;

        $exists = false;
        $yith_wcwl_items = YITH_WCWL_ITEMS_TABLE;
        if( $user_id ) {
    	    $sql = "SELECT COUNT(*) as `cnt` FROM $yith_wcwl_items WHERE `prod_id` = %d AND `user_id` = %d";
    	    $sql_args = array(
    		    $product_id,
    		    $user_id
    	    );

    	    if( $wishlist_id != false ){
    		    $sql .= " AND `wishlist_id` = %d";
    		    $sql_args[] = $wishlist_id;
    	    }
    	    elseif( $default_wishlist_id = $this->pgs_woo_api_generate_default_wishlist( $user_id ) ){
    		    $sql .= " AND `wishlist_id` = %d";
    		    $sql_args[] = $default_wishlist_id;
    	    }
    	    else{
    		    $sql .= " AND `wishlist_id` IS NULL";
    	    }

    	    $results = $wpdb->get_var( $wpdb->prepare( $sql, $sql_args ) );
    	    $exists = (bool) ( $results > 0 );
            return apply_filters( 'yith_wcwl_pgs_woo_api_is_product_in_wishlist', $exists, $product_id, $wishlist_id );
        } else {
            return false;
        }

    }

    private function pgs_is_product_exists($prod_id){
        global $wpdb;
        $hidden_products = $this->pgs_get_hidden_products();
        $res = true;

        if(!in_array($prod_id,$hidden_products)){
            $result = array();
            $sql = "SELECT ID FROM {$wpdb->posts} WHERE ID = $prod_id AND post_type = 'product' AND post_status = 'publish'";
            $result = $wpdb->get_row( $sql, ARRAY_A );
            if(empty($result)){
                $res = false;
            }
        } else {
            $res = false;
        }
        return $res;
    }


    private function pgs_woo_api_get_wishlist_detail( $wishlist_id ) {
        global $wpdb;
        $yith_wcwl_wishlists = YITH_WCWL_WISHLISTS_TABLE;
        $sql = "SELECT * FROM $yith_wcwl_wishlists WHERE `ID` = %d";
        return $wpdb->get_row( $wpdb->prepare( $sql, $wishlist_id ), ARRAY_A );
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/add_wishlist
    * @param user_id: ####
    * @param product_id: ####
    */
    public function pgs_woo_api_add_wishlist(){


        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'user_id','product_id' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }


        $prod_id = ( isset( $request['product_id'] ) && is_numeric( $request['product_id'] ) ) ? $request['product_id'] : false;
        $wishlist_id = ( isset( $request['wishlist_id'] ) && strcmp( $request['wishlist_id'], 0 ) != 0 ) ? $request['wishlist_id'] : false;
        $quantity = ( isset( $request['quantity'] ) ) ? ( int ) $request['quantity'] : 1;
        $wishlist_name = ( ! empty( $request['wishlist_name'] ) ) ? $request['wishlist_name'] : '';
        $user_id = ( ! empty( $request['user_id'] ) ) ? $request['user_id'] : false;
        return $this->pgs_add_to_list($prod_id,$wishlist_id,$quantity,$wishlist_name,$user_id);
    }

    /**
     *
     * Add to wishlist
     */
    private function pgs_add_to_list($prod_id,$wishlist_id,$quantity,$wishlist_name,$user_id){
        global $wpdb;
        $error = array( "status" => "error" );
        $result = false;
        $last_operation_token = false;
        do_action( 'yith_wcwl_adding_to_wishlist', $prod_id, $wishlist_id, $user_id );

        // filtering params
        $prod_id = apply_filters( 'yith_wcwl_adding_to_wishlist_prod_id', $prod_id );
        $wishlist_id = apply_filters( 'yith_wcwl_adding_to_wishlist_wishlist_id', $wishlist_id );
        $quantity = apply_filters( 'yith_wcwl_adding_to_wishlist_quantity', $quantity );
        $user_id = apply_filters( 'yith_wcwl_adding_to_wishlist_user_id', $user_id );
        $wishlist_name = apply_filters( 'yith_wcwl_adding_to_wishlist_wishlist_name', $wishlist_name );

        $error = array( "status" => "error" );
        if ( $prod_id == false ) {
            $error['message'] = __( 'An error occurred while adding products to the wishlist.', 'pgs-woo-api' );
            return $error;

        }

        //check for existence,  product ID, variation ID, variation data, and other cart item data
        if( strcmp( $wishlist_id, 'new' ) != 0 && $this->pgs_woo_api_is_product_in_wishlist( $prod_id, $wishlist_id,$user_id ) ) {
            if( $wishlist_id != false ){
                $wishlist = $this->pgs_woo_api_get_wishlist_detail( $wishlist_id );
                $last_operation_token = $wishlist['wishlist_token'];
            }
            else{
                $last_operation_token = false;
            }
            $error['message'] = esc_html__("exists","pgs-woo-api");
            return $error;

        }

        if( $user_id != false ) {

            $insert_args = array(
                'prod_id' => $prod_id,
                'user_id' => $user_id,
                'quantity' => $quantity,
                'dateadded' => date( 'Y-m-d H:i:s' )
            );

            if( ! empty( $wishlist_id ) && strcmp( $wishlist_id, 'new' ) != 0 ){
                $insert_args[ 'wishlist_id' ] = $wishlist_id;

                $wishlist = $this->pgs_woo_api_get_wishlist_detail( $insert_args[ 'wishlist_id' ] );
                $last_operation_token = $wishlist['wishlist_token'];
            }
            elseif( strcmp( $wishlist_id, 'new' ) == 0 ){
                $response = $this->pgs_woo_add_wishlist($user_id);

                if( $response == "error" ){
                    return "error";
                }
                else{
                    $insert_args[ 'wishlist_id' ] = $response;

                    $wishlist = $this->pgs_woo_api_get_wishlist_detail( $insert_args[ 'wishlist_id' ] );
                    $last_operation_token = $wishlist['wishlist_token'];
                }
            }
            elseif( empty( $wishlist_id ) ){
                $wishlist_id = $this->pgs_woo_api_generate_default_wishlist( $user_id );
                $insert_args[ 'wishlist_id' ] = $wishlist_id;

                if( $this->pgs_woo_api_is_product_in_wishlist( $prod_id, $wishlist_id, $user_id ) ){

                    $error['message'] = "exists";
                    return $error;

                }
            }
            $is_product = $this->pgs_is_product_exists($prod_id);
            if($is_product){
                $yith_wcwl_items = YITH_WCWL_ITEMS_TABLE;
                $result = $wpdb->insert( $yith_wcwl_items, $insert_args );
            }
            if( $result ){
                if( $last_operation_token ) {
                    delete_transient( 'yith_wcwl_wishlist_count_' . $last_operation_token );
                }

                if( $user_id ) {
                    delete_transient( 'yith_wcwl_user_default_count_' . $user_id );
                    delete_transient( 'yith_wcwl_user_total_count_' . $user_id );
                }
            }
        }


        $wishlists = $this->pgs_woo_api_all_wishlists( array(
            'user_id' => $user_id,
            'is_default' => 1
        ) );
        $res = array();
        if( $result ) {
            do_action( 'yith_wcwl_added_to_wishlist', $prod_id, $wishlist_id, $user_id );
            $res['status'] = "success";
            $res['message'] = esc_html__( "Product added successfully", 'pgs-woo-api' );
            $res['sync_list'] = $wishlists;
            return $res;

        }
        else {
            $error['message'] = esc_html__('An error occurred while adding products to wishlist.', 'pgs-woo-api');
            $error['sync_list'] = $wishlists;
            return $error;

        }
    }

    private function pgs_woo_add_wishlist($user_id) {
        $error = array( "status" => "error" );
        if( $user_id == false ){
            $errors['message'] = esc_html__( 'You need to login before creating a new wishlist', 'pgs-woo-api' );
            return $errors;
        } else {
            return $this->pgs_woo_api_generate_default_wishlist( $user_id );
        }
    }

    private function pgs_woo_api_generate_default_wishlist( $user_id ){
        global $wpdb;
        $yith_wcwl_items = YITH_WCWL_ITEMS_TABLE;
        $yith_wcwl_wishlists = YITH_WCWL_WISHLISTS_TABLE;
        $wishlists = $this->pgs_woo_api_get_wishlists( array(
            'user_id' => $user_id,
            'is_default' => 1
        ) );

        if( ! empty( $wishlists ) ){
            $default_user_wishlist = $wishlists[0]['ID'];
            $last_operation_token = $wishlists[0]['wishlist_token'];
        }
        else{
            $token = $this->pgs_woo_api_generate_wishlist_token();
            $last_operation_token = $token;

            $wpdb->insert( $yith_wcwl_wishlists, array(
                'user_id' => apply_filters( 'yith_wcwl_default_wishlist_user_id', $user_id ),
                'wishlist_slug' => apply_filters( 'yith_wcwl_default_wishlist_slug', '' ),
                'wishlist_token' => $token,
                'wishlist_name' => apply_filters( 'yith_wcwl_default_wishlist_name', '' ),
                'wishlist_privacy' => apply_filters( 'yith_wcwl_default_wishlist_privacy', 0 ),
                'is_default' => 1
            ) );

            $default_user_wishlist = $wpdb->insert_id;
        }

        $sql = "UPDATE $yith_wcwl_items SET wishlist_id = %d WHERE user_id = %d AND wishlist_id IS NULL";
        $sql_args = array(
            $default_user_wishlist,
            $user_id
        );

        $wpdb->query( $wpdb->prepare( $sql, $sql_args ) );

        return $default_user_wishlist;
    }

    private function pgs_woo_api_generate_wishlist_token(){
        global $wpdb;
        $count = 0;

        $yith_wcwl_wishlists = YITH_WCWL_WISHLISTS_TABLE;
        $sql = "SELECT COUNT(*) FROM $yith_wcwl_wishlists WHERE `wishlist_token` = %s";

        do {
            $dictionary = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            $nchars = 12;
            $token = "";

            for( $i = 0; $i <= $nchars - 1; $i++ ){
                $token .= $dictionary[ mt_rand( 0, strlen( $dictionary ) - 1 ) ];
            }

            $count = $wpdb->get_var( $wpdb->prepare( $sql, $token ) );
        }
        while( $count != 0 );

        return $token;
    }


    /**
    * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/remove_wishlist
    * @param user_id: email
    * @param product_id: password
    */
    public function pgs_woo_api_remove_wishlist(){

        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        $required = array( 'user_id','product_id' );

        $validation = $this->pgs_woo_api_param_validation( $required, $request );
        if($validation){
           return $validation;
        }


        return $this->pgs_woo_api_remove_to_list();
    }


    /**
     *
     * Remove to wishlist
     */
    private function pgs_woo_api_remove_to_list() {

        $input = file_get_contents("php://input");
        $obj = json_decode($input,true);

        global $wpdb;
        $error = array( "status" => "error" );
        $prod_id = ( isset( $obj['product_id'] ) && is_numeric( $obj['product_id'] ) ) ? $obj['product_id'] : false;
        $wishlist_id = ( isset( $obj['wishlist_id'] ) && is_numeric( $obj['wishlist_id'] ) ) ? $obj['wishlist_id'] : false;
        $user_id = ( ! empty( $obj['user_id'] ) ) ? $obj['user_id'] : false;

        if( $prod_id == false ){
            return false;
        }
        $yith_wcwl_items = YITH_WCWL_ITEMS_TABLE;
        if ( $user_id ) {
            $sql = "DELETE FROM $yith_wcwl_items WHERE user_id = %d AND prod_id = %d";
            $sql_args = array(
                $user_id,
                $prod_id
            );

            if( empty( $wishlist_id ) ){
                $wishlist_id = $this->pgs_woo_api_generate_default_wishlist( $user_id );
            }

            $wishlist = $this->pgs_woo_api_get_wishlist_detail( $wishlist_id );
            $last_operation_token = $wishlist['wishlist_token'];

            $sql .= " AND wishlist_id = %d";
            $sql_args[] = $wishlist_id;

            $result = $wpdb->query( $wpdb->prepare( $sql, $sql_args ) );


            $wishlists = $this->pgs_woo_api_all_wishlists( array(
                'user_id' => $user_id,
                'is_default' => 1
            ) );
            $res = array();
            if ( $result ) {
                if( $last_operation_token ) {
                    delete_transient( 'yith_wcwl_wishlist_count_' . $last_operation_token );
                }

                if( $user_id ) {
                    delete_transient( 'yith_wcwl_user_default_count_' . $user_id );
                    delete_transient( 'yith_wcwl_user_total_count_' . $user_id );
                }

                $res['status'] = "success";
                $res['message'] = esc_html__("Product deleted successfully","pgs-woo-api");
                $res['sync_list'] = $wishlists;
                return $res;
            }
            else {
                $error['message'] = esc_html__( 'An error occurred while removing products from the wishlist', 'pgs-woo-api' );
                $error['sync_list'] = $wishlists;
                return $error;

            }
        }
    }

    private function pgs_woo_api_get_wishlists( $args = array() ){
        global $wpdb;
        $yith_wcwl_items = YITH_WCWL_ITEMS_TABLE;
        $yith_wcwl_wishlists = YITH_WCWL_WISHLISTS_TABLE;

        $default = array(
            'id' => false,
            'user_id' => false,
            'wishlist_slug' => false,
            'wishlist_name' => false,
            'wishlist_token' => false,
            'wishlist_visibility' => 'all', // all, visible, public, shared, private
            'user_search' => false,
            'is_default' => false,
            'orderby' => 'ID',
            'order' => 'DESC',
            'limit' =>  false,
            'offset' => 0,
            'show_empty' => true
        );

        $args = wp_parse_args( $args, $default );
        extract( $args );

        $sql = "SELECT l.*";

        if( ! empty( $user_search ) ){
            $sql .= ", u.user_email, umn.meta_value AS first_name, ums.meta_value AS last_name";
        }

        $sql .= " FROM $yith_wcwl_wishlists AS l";

        if( ! empty( $user_search ) || ( ! empty($orderby ) && $orderby == 'user_login' ) ) {
            $sql .= " LEFT JOIN `{$wpdb->users}` AS u ON l.`user_id` = u.ID";
        }

        if( ! empty( $user_search ) ){
            $sql .= " LEFT JOIN `{$wpdb->usermeta}` AS umn ON umn.`user_id` = u.`ID`";
            $sql .= " LEFT JOIN `{$wpdb->usermeta}` AS ums ON ums.`user_id` = u.`ID`";
        }

        $sql .= " WHERE 1";

        if( ! empty( $user_id ) ){
            $sql .= " AND l.`user_id` = %d";

            $sql_args = array(
                $user_id
            );
        }

        if( ! empty( $user_search ) ){
            $sql .= " AND ( umn.`meta_key` LIKE %s AND ums.`meta_key` LIKE %s AND ( u.`user_email` LIKE %s OR umn.`meta_value` LIKE %s OR ums.`meta_value` LIKE %s ) )";
            $sql_args[] = 'first_name';
            $sql_args[] = 'last_name';
            $sql_args[] = "%" . $user_search . "%";
            $sql_args[] = "%" . $user_search . "%";
            $sql_args[] = "%" . $user_search . "%";
        }

        if( ! empty( $is_default ) ){
            $sql .= " AND l.`is_default` = %d";
            $sql_args[] = $is_default;
        }

        if( ! empty( $id ) ){
            $sql .= " AND l.`ID` = %d";
            $sql_args[] = $id;
        }

        if( isset( $wishlist_slug ) && $wishlist_slug !== false ){
            $sql .= " AND l.`wishlist_slug` = %s";
            $sql_args[] = sanitize_title_with_dashes( $wishlist_slug );
        }

        if( ! empty( $wishlist_token ) ){
            $sql .= " AND l.`wishlist_token` = %s";
            $sql_args[] = $wishlist_token;
        }

        if( ! empty( $wishlist_name ) ){
            $sql .= " AND l.`wishlist_name` LIKE %s";
            $sql_args[] = "%" . $wishlist_name . "%";
        }

        if( ! empty( $wishlist_visibility ) && $wishlist_visibility != 'all' ){
            switch( $wishlist_visibility ){
                case 'visible':
                    $sql .= " AND ( l.`wishlist_privacy` = %d OR l.`is_public` = %d )";
                    $sql_args[] = 0;
                    $sql_args[] = 1;
                    break;
                case 'public':
                    $sql .= " AND l.`wishlist_privacy` = %d";
                    $sql_args[] = 0;
                    break;
                case 'shared':
                    $sql .= " AND l.`wishlist_privacy` = %d";
                    $sql_args[] = 1;
                    break;
                case 'private':
                    $sql .= " AND l.`wishlist_privacy` = %d";
                    $sql_args[] = 2;
                    break;
                default:
                    $sql .= " AND l.`wishlist_privacy` = %d";
                    $sql_args[] = 0;
                    break;
            }
        }

        if( empty( $show_empty ) ){
            $sql .= " AND l.`ID` IN ( SELECT wishlist_id FROM $yith_wcwl_items )";
        }

        if( ! empty( $orderby ) && isset( $order ) ) {
            $sql .= " ORDER BY " . $orderby . " " . $order;
        }

        if( ! empty( $limit ) && isset( $offset ) ){
            $sql .= " LIMIT " . $offset . ", " . $limit;
        }

        if( ! empty( $sql_args ) ){
            $sql = $wpdb->prepare( $sql, $sql_args );
        }

        $lists = $wpdb->get_results( $sql, ARRAY_A );

        return $lists;
    }

 }
 new PGS_WOO_API_WishlistController;