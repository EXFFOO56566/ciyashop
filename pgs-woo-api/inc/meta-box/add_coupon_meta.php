<?php
function add_coupon_push_notification_checkbox() {
    woocommerce_wp_checkbox( array( 'id' => 'hide_coupon_on_app', 'label' => esc_html__( 'Hide coupon', 'pgs-woo-api' ), 'description' => sprintf( __( 'Hide this coupon on Application', 'pgs-woo-api' ) ) ) );
    woocommerce_wp_checkbox( array( 'id' => 'send_notification_coupon', 'label' => esc_html__( 'Push notification', 'pgs-woo-api' ), 'description' => sprintf( __( 'Send push notification to all users', 'pgs-woo-api' ) ) ) );
}
add_action( 'woocommerce_coupon_options', 'add_coupon_push_notification_checkbox', 10, 0 );

function save_coupon_push_notification_checkbox( $post_id ) {

    $hide_coupon_on_app = isset( $_POST['hide_coupon_on_app'] ) ? 'yes' : 'no';
    update_post_meta( $post_id, 'hide_coupon_on_app', $hide_coupon_on_app );

    $send_notification_coupon = isset( $_POST['send_notification_coupon'] ) ? 'yes' : 'no';
    update_post_meta( $post_id, 'send_notification_coupon', $send_notification_coupon );
    if($hide_coupon_on_app != 'yes'){
        if($send_notification_coupon == 'yes'){

            $data = array();
            $push_status = pgs_woo_api_push_status();
            if($push_status){
                $data = pgs_woo_api_get_push_notification_data(0);
                if(!empty($data)){
                    $coupon_code = get_the_title($post_id);
                    $device_data = array();
                    foreach($data as $val){
                        $device_data[] = array(
                            'token' => $val->device_token,
                            'type' => $val->device_type
                        );
                    }
                    $notification_code = 1;//for order status change
                    $not_data = pgs_woo_api_get_notification_data($notification_code);

                    if(!empty($not_data)){
                        $title = $not_data['title'];
                        $message = $not_data['message'];
                        $message = str_replace('{{coupon}}',$coupon_code,$message);
                        if(empty($title)){
                            $title = get_bloginfo('name');
                        }
                        if(empty($not_data['message'])){
                            $message = get_the_excerpt($post_id);
                        }
                        $msg = $title;
                        $custom_msg = $message;
                        $badge = 0;
                        $push = new PGS_WOO_API_Controller;
                        $push->send_push( $msg, $badge, $custom_msg,$notification_code,$device_data);
                    }
                }
            }
        }
    }
}
add_action( 'woocommerce_coupon_options_save', 'save_coupon_push_notification_checkbox');
