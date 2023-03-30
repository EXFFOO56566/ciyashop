<?php
add_action( 'product_cat_add_form_fields', 'pgs_woo_api_taxonomy_add_new_meta_field', 999, 2 );
add_action( 'product_cat_edit_form_fields', 'pgs_woo_api_taxonomy_edit_new_meta_field', 999, 2 );

add_action( 'create_product_cat', 'save_custom_tax_field' );
add_action( 'edited_product_cat', 'save_custom_tax_field' );

// Add term page
function pgs_woo_api_taxonomy_add_new_meta_field($term) {
	// this will add the custom meta field to the add new term page
	$vsrc = array();$app_cat='';
	if(isset($term->term_id) && !empty($term->term_id)){
		$termID = $term->term_id;
		$app_cat = $termMeta['product_app_cat_thumbnail_id'];
		$vsrc = wp_get_attachment_image_src($app_cat, 'thumbnail' );

	}
	?>
	<div class="form-field term-thumbnail-wrap">
		<label><?php esc_html_e('App Category Thumbnail','pgs-woo-api')?></label>
		<div class="ciyashop-app-product-cat-thumb-wrapper">
			<div id="product_app_cat_thumbnail" class="upload_image" style="float: left; margin-right: 10px;">
				<?php
				$display = "none;";
				if(!empty($vsrc)){
					$display = "inline-block;";
					?>
					<img src="<?php echo esc_url($vsrc[0])?>" width="60px" height="60px">
				<?php
				}else{?>
					<img src="<?php echo PGS_API_URL.'/img/pgs_app_cat_placeholder.jpg'?>" width="60px" height="60px">
				<?php }
				?>
			</div>
			<div style="line-height: 60px;">
				<input type="hidden" id="product_app_cat_thumbnail_id" class="upload_image_id" name="product_app_cat_thumbnail_id" value="<?php echo esc_attr($app_cat)?>">
				<button type="button" id="upload-image-button" class="upload_app_image_button button"><?php esc_html_e('Upload/Add App cat image','pgs-woo-api')?></button>
				<button type="button" id="remove-image-button" class="remove_app_image_button button" style="display: <?php echo esc_attr($display);?>;"><?php esc_html_e('Remove image','pgs-woo-api')?></button>
			</div>
			<div class="clear"></div>
			<p class="description"><?php _e( 'Add mobile application category icon image','pgs-woo-api' ); ?></p>
		</div>
	</div>
	<?php
}

function pgs_woo_api_taxonomy_edit_new_meta_field( $term ) {

	// this will add the custom meta field to the add new term page
	$vsrc = array();
	if(isset($term->term_id) && !empty($term->term_id)){
		$termID = $term->term_id;
		$product_app_cat_thumbnail_id = get_term_meta($termID, 'product_app_cat_thumbnail_id', true);
		$vsrc = wp_get_attachment_image_src($product_app_cat_thumbnail_id, 'thumbnail' );
	}
	?>
	<table class="form-table" role="presentation">
		<tr class="form-field">
			<th scope="row" valign="top"><label><?php esc_html_e('App Category Thumbnail','pgs-woo-api')?></label></th>
			<td>
				<div class="ciyashop-app-product-cat-thumb-wrapper">
					<div class="upload_image" id="product_app_cat_thumbnail" style="float: left; margin-right: 10px;">
						<?php
						$display = "none;";
						if(!empty($vsrc)){
							$display = "inline-block;";
							?>
							<img src="<?php echo esc_url($vsrc[0])?>" width="60px" height="60px">
						<?php
						}else{?>
							<img src="<?php echo PGS_API_URL.'/img/pgs_app_cat_placeholder.jpg'?>" width="60px" height="60px">
						<?php }
						?>
					</div>
					<div style="line-height: 60px;" class="clearfix">
						<input type="hidden" id="product_app_cat_thumbnail_id" class="upload_image_id" name="product_app_cat_thumbnail_id" value="<?php echo esc_attr($product_app_cat_thumbnail_id)?>">
						<button type="button" id="upload-image-button" class="upload_app_image_button button"><?php esc_html_e('Upload/Add App cat image','pgs-woo-api')?></button>
						<button type="button" id="remove-image-button" class="remove_app_image_button button" style="display: <?php echo esc_attr($display);?>"><?php esc_html_e('Remove image','pgs-woo-api')?></button>
					</div>
					<p class="description"><?php esc_html_e( 'Add mobile application category image','pgs-woo-api' ); ?></p>
					<div class="clear"></div>
				</div>
			</td>
		</tr>
	</table>
	<?php
}

function save_custom_tax_field( $termID ) {
	if ( isset( $_POST['product_app_cat_thumbnail_id'] ) ) {
		$product_app_cat_thumbnail_id = isset( $_POST['product_app_cat_thumbnail_id'] ) ? $_POST['product_app_cat_thumbnail_id'] : '';
		update_term_meta($termID, 'product_app_cat_thumbnail_id', $product_app_cat_thumbnail_id);
	}
}



/* ---------------------------------------------------------------------------
 * Edit columns
 * --------------------------------------------------------------------------- */
function pgs_woo_api_product_cat_edit_columns($columns)
{
	$newcolumns = array(
		"cb" => "<input type='checkbox' />",
		"app_image" => esc_html__( 'App Image', 'pgs-woo-api' ),
	);
	$columns = array_merge($newcolumns, $columns);

	return $columns;
}
add_filter("manage_edit-product_cat_columns", "pgs_woo_api_product_cat_edit_columns",10,1);
/* ---------------------------------------------------------------------------
 * Custom columns
 * --------------------------------------------------------------------------- */
function pgs_woo_api_product_cat_add_columns($content, $column_name, $term_id) {

	$vsrc = array();
	if($column_name == "app_image"){
		if(isset($term_id) && !empty($term_id)){
			$termID = $term_id;
			$product_app_cat_thumbnail_id = get_term_meta($termID, 'product_app_cat_thumbnail_id', true);
			$vsrc = wp_get_attachment_image_src($product_app_cat_thumbnail_id, 'thumbnail' );

			if(!empty($vsrc)){
				$img = '<img src="'.esc_url($vsrc[0]).'" width="40px" height="40px">';

			}else{
				$img = '<img src="'.PGS_API_URL.'/img/pgs_app_cat_placeholder.jpg" width="40px" height="40px">';
			}
			return $img;
		}
	}
}
add_filter( 'manage_product_cat_custom_column', 'pgs_woo_api_product_cat_add_columns', 10, 3 );
