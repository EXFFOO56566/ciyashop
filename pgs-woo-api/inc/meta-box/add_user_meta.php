<?php
/**
 * Meta box display callback.
 *
 * @param WP_Post $post Current post object.
 */
function pgs_additional_profile_fields( $user ) {

    wp_enqueue_style( 'jquery-ui' );
    wp_enqueue_script('pgs-woo-api-js');

    $role = (isset($user->roles[0]) && !empty($user->roles[0]))?$user->roles[0]:'notadmin';
    if( $role == 'customer'){
        $status = get_user_meta($user->ID,'pgs_woo_api_disable_user',true);
        $gender = get_user_meta($user->ID,'gender',true);
        $dob = get_user_meta($user->ID,'dob',true);
        $mobile = get_user_meta($user->ID,'mobile',true);
        ?>
        <table class="form-table">
            <tbody>
                <tr class="user-pass1-wrap">
            		<th><label><?php esc_html_e('Mobile','pgs-woo-api')?></label></th>
            		<td>
            			<label>
                            <input type="text" name="mobile" class="regular-text" value="<?php echo (isset($mobile))?$mobile:'';?>" />
            			</label>
            		</td>
            	</tr>
                <tr class="user-pass1-wrap">
            		<th><label><?php esc_html_e('Gender','pgs-woo-api')?></label></th>
            		<td>
            			<label>
                            <input type="radio" name="gender" value="male" <?php echo (isset($gender)&& $gender == "male")?"checked=''":'';?> /> <?php esc_html_e('Male','pgs-woo-api')?>
                        </label>
                        <label>
                            <input type="radio" name="gender" value="female" <?php echo (isset($gender)&& $gender == "female")?"checked=''":'';?> /> <?php esc_html_e('Female','pgs-woo-api')?>
            			</label>
            		</td>
            	</tr>
                <tr class="user-pass1-wrap">
            		<th><label><?php esc_html_e('DOB','pgs-woo-api')?></label></th>
            		<td>
            			<label>
                            <input type="text" name="dob"  id="dob" class="regular-text" value="<?php echo (isset($dob))?$dob:'';?>" />
            			</label>
            		</td>
            	</tr>
                <tr class="user-pass1-wrap">
            		<th><label><?php esc_html_e('Deactivate User','pgs-woo-api')?></label></th>
            		<td>
            			<label>
                            <input type="checkbox" name="pgs_woo_api_disable_user" class="comment_shortcuts" value="1" <?php echo ($status == "1")?"checked=''":''?>  /> <?php esc_html_e('User can\'t login if deactivate','pgs-woo-api')?>
            			</label>
            		</td>
            	</tr>
            </tbody>
        </table>
        <?php
    }
}

/**
 * Save meta box content.
 *
 * @param int $post_id Post ID
 */
function pgs_save_profile_fields( $user_id ) {
    if ( ! current_user_can( 'edit_user', $user_id ) ) {
   	 return false;
    }

    if ( empty( $_POST['pgs_woo_api_disable_user'] ) ) {
   	    update_user_meta( $user_id, 'pgs_woo_api_disable_user', 0 );
    } else {
        update_user_meta( $user_id, 'pgs_woo_api_disable_user', $_POST['pgs_woo_api_disable_user'] );
    }

    if ( empty( $_POST['pgs_user_image_id'] ) ) {
   	    update_user_meta( $user_id, 'pgs_user_image_id', 0 );
        $user_image = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
        update_user_meta( $user_id, 'pgs_user_image', $user_image );
    } else {
        $pgs_user_image_id = $_POST['pgs_user_image_id'];
        update_user_meta( $user_id, 'pgs_user_image_id', $pgs_user_image_id );
        if(isset($pgs_user_image_id) && !empty($pgs_user_image_id) ){
            $src = wp_get_attachment_image_src($pgs_user_image_id, 'thumbnail' );
            if(!empty($src)){
                $user_image = esc_url($src[0]);
            }
        }else {
            $user_image = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
        }
        update_user_meta( $user_id, 'pgs_user_image', $user_image );
    }

    if ( empty( $_POST['mobile'] ) ) {
   	    update_user_meta( $user_id, 'mobile', '' );
    } else {
        update_user_meta( $user_id, 'mobile', $_POST['mobile'] );
    }

    if ( empty( $_POST['gender'] ) ) {
   	    update_user_meta( $user_id, 'gender', '' );
    } else {
        update_user_meta( $user_id, 'gender', $_POST['gender'] );
    }

    if ( empty( $_POST['dob'] ) ) {
   	    update_user_meta( $user_id, 'dob', '' );
    } else {
        update_user_meta( $user_id, 'dob', $_POST['dob'] );
    }

}

add_action( 'show_user_profile', 'pgs_additional_profile_fields' );
add_action( 'edit_user_profile', 'pgs_additional_profile_fields' );

add_action( 'personal_options_update', 'pgs_save_profile_fields' );
add_action( 'edit_user_profile_update', 'pgs_save_profile_fields' );

/**
 * Show the new image field in the user profile page.
 *
 * @param object $user User object.
 */
function pgs_profile_img_fields( $user ) {

    if ( ! current_user_can( 'upload_files' ) ) {
		return;
	}
	$pgs_user_image_id = 0;
    $pgs_user_image_id = get_user_meta( $user->ID, 'pgs_user_image_id',true );
	$src = get_user_meta( $user->ID, 'pgs_user_image',true );
    $role = (isset($user->roles[0]) && !empty($user->roles[0]))?$user->roles[0]:'notadmin';
    if( $role == 'customer'){
    ?>

	<div id="pgs_container">
		<h3><?php esc_html_e( 'Custom User Profile Image', 'pgs-woo-api' ); ?></h3>

		<table class="form-table">
			<tr>
				<th><label for="pgs_meta"><?php esc_html_e( 'Profile Image', 'pgs-woo-api' ); ?></label></th>
				<td>
                    <div  class="pgs-woo-api-field-group">
                        <div class="pgs-woo-api-form-group">
                                <!-- Hold the value here if this is a WPMU image -->
        						<?php
                                if(isset($src) && !empty($src) ){
                                    $imgurl = $src;
                                } else {
                                    $imgurl = esc_url( PGS_API_URL.'img/pgs_user_placeholder.jpg' );
                                }?>
                                <div class="upload_image">
                                    <img src="<?php echo esc_url($imgurl)?>" alt="No image" width="150px" height="150px" />
                                </div>
        						<input type="hidden" name="pgs_user_image_id" id="pgs_user_image_id" class="upload_image_id" value="<?php echo esc_attr( $pgs_user_image_id ); ?>"/>
        						<br/>
                        </div>
                        <input id="uploadimage-btn" type='button' class="upload-image-button button-primary" value="<?php esc_attr_e( "Upload",'pgs-woo-api')?>"/>
                        <input id="uploadimage-btn" type='button' class="remove-image-button button-primary" value="<?php esc_attr_e( "Remove",'pgs-woo-api')?>" style="<?php echo ( ! $pgs_user_image_id ? 'display:none;' : '' ); ?>"/>
                    </div>

					<p class="description">
						<?php esc_html_e( 'Upload a custom image for your user profile','pgs-woo-api'); ?>
					</p>
				</td>
			</tr>
		</table><!-- end form-table -->
	</div> <!-- end #pgs_container -->

	<?php
    }
	// Enqueue the WordPress Media Uploader.
	wp_enqueue_media();
    wp_enqueue_script('pgs-woo-api-js');
}

add_action( 'show_user_profile', 'pgs_profile_img_fields' );
add_action( 'edit_user_profile', 'pgs_profile_img_fields' );
