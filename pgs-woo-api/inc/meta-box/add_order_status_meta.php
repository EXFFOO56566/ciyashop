<?php
function pgs_woo_api_send_push_notification_on_update_order_status( $post_id, $post, $update ) {
    $post_type = get_post_type($post_id);
    $send_notification = '';

    if ( "shop_order" == $post_type ){
        $post_status = get_post_status( $post_id );

        if($post->post_status != $post_status){


            $wc_statuses = array();
            $wc_statuses = wc_get_order_statuses();
            $label = (isset($wc_statuses[$post_status]))?$wc_statuses[$post_status]:'';

            $data = array();
            $push_status = pgs_woo_api_push_status();
            if($push_status){

                $user_id = (isset($_POST['customer_user']))?$_POST['customer_user']:0;
                if(isset($user_id) && $user_id > 0){
                    $data = pgs_woo_api_get_push_notification_data($user_id);
                    if(!empty($data)){

                        $device_data = array();
                        foreach($data as $val){
                            $device_data[] = array(
                                'token' => $val->device_token,
                                'type' => $val->device_type
                            );
                        }

                        $notification_code = 2;//for order status change
                        $not_data = pgs_woo_api_get_notification_data($notification_code);
                        if(!empty($not_data)){
                            $title = $not_data['title'];
                            $message = $not_data['message'];
                            $message = str_replace('{{status}}',$label,$message);
                            $message = str_replace('{{order_id}}',$post_id,$message);
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
}
add_action( 'save_post', 'pgs_woo_api_send_push_notification_on_update_order_status', 10, 3 );
