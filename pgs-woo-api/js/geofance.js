/**
* App Custom JS
* @param-upic:
*/
var selectedShape;
function geoFenc() {
    var dblat = 19.0822507;
    var dblng = 72.881204;
    var dbradius = 0;
    var dbzoom = 12;
    if(parseFloat(pgs_woo_api_map.lat)){
        dblat = parseFloat(pgs_woo_api_map.lat);
        dblng = parseFloat(pgs_woo_api_map.lng);
        dbradius = parseFloat(pgs_woo_api_map.radius);
        dbzoom = parseInt(pgs_woo_api_map.zoom);
    }

    var center = {lat: dblat, lng: dblng};
    var map = new google.maps.Map(document.getElementById('map'), {
        center: center,
        zoom: dbzoom
    });
    if(pgs_woo_api_map.dbradius != 0){
        var circle = new google.maps.Circle({
            center: center,
            map: map,
            radius: dbradius,
            fillColor: '#000',
            fillOpacity: 0.4,
            strokeWeight: 1,
            editable: true,
            zIndex: 1
        });
        selectedShape = circle;
    }
    var drawingManager = new google.maps.drawing.DrawingManager({
            drawingMode: google.maps.drawing.OverlayType.POINTER,
            drawingControl: true,
            drawingControlOptions: {
            position: google.maps.ControlPosition.TOP_CENTER,
            drawingModes: ['circle']
        },
        circleOptions: {
            fillColor: '#000',
            fillOpacity: 0.4,
            strokeWeight: 1,
            editable: true,
            zIndex: 1
        }
    });
    drawingManager.setMap(map);

    google.maps.event.addListener(drawingManager, 'circlecomplete', function(circle) {
        updateRadius(circle);
    });

    google.maps.event.addListener(circle, 'radius_changed', function () {
		updateRadius(circle);
	});
	google.maps.event.addListener(circle, 'center_changed', function () {
		updateRadius(circle);
	});
    google.maps.event.addListener(circle, 'zoom_changed', function () {
		updateRadius(circle);
	});

    map.addListener('zoom_changed', function() {
          upDateZoom(map);
    });

    google.maps.event.addListener(drawingManager, 'overlaycomplete', function(event) {
      if (event.type == 'circle') {
        updateRadius(event.overlay);
        drawingManager.setDrawingMode(null);
        var newShape = event.overlay;
		newShape.type = event.type;
		google.maps.event.addListener(newShape, 'click', function() {
		   setSelection(newShape);
		});
		setSelection(newShape);
      }
    });
    google.maps.event.addListener(drawingManager, 'drawingmode_changed', function(){
        if ((drawingManager.getDrawingMode() == google.maps.drawing.OverlayType.CIRCLE) && (selectedShape != null))
                selectedShape.setMap(null);

    });
    google.maps.event.addListener(map, 'click', clearSelection);

    // Create the search box and link it to the UI element.
    var input = document.getElementById('pac-input');
    var searchBox = new google.maps.places.SearchBox(input);


    // Bias the SearchBox results towards current map's viewport.
    map.addListener('bounds_changed', function() {
        searchBox.setBounds(map.getBounds());
    });

    var markers = [];
    // Listen for the event fired when the user selects a prediction and retrieve
    // more details for that place.

    searchBox.addListener('places_changed', function() {
        var places = searchBox.getPlaces();

        if (places.length == 0) {
            return;
        }

        // Clear out the old markers.
        markers.forEach(function(marker) {
            marker.setMap(null);
        });
        markers = [];

        // For each place, get the icon, name and location.
        var bounds = new google.maps.LatLngBounds();
        places.forEach(function(place) {
            if (!place.geometry) {
                console.log("Returned place contains no geometry");
                return;
            }
            var icon = {
                url: place.icon,
                size: new google.maps.Size(71, 71),
                origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(17, 34),
                scaledSize: new google.maps.Size(25, 25)
            };

            // Create a marker for each place.
            markers.push(new google.maps.Marker({
                map: map,
                title: place.name,
                position: place.geometry.location
            }));

            if (place.geometry.viewport) {
                // Only geocodes have viewport.
                bounds.union(place.geometry.viewport);
            } else {
                bounds.extend(place.geometry.location);
            }
        });
        map.fitBounds(bounds);
    });

}
function clearSelection() {
	if (selectedShape) {
		selectedShape.setEditable(false);
		selectedShape = null;
	}
}

function setSelection(shape) {
	clearSelection();
	selectedShape = shape;
	shape.setEditable(true);
}

function upDateZoom(map){
    var zoomlev = document.getElementById('zoom');
        zoomlev.value = map.getZoom();
}

function updateRadius(circle){
    var radius = circle.getRadius();
	var lats = circle.getCenter().lat();
	var lngs = circle.getCenter().lng();
	//var zoom = circle.getZoom();
    //console.log('zoom'+zoom);
	var radiusID = document.getElementById('radius');
    radiusID.value = radius;
    var latID = document.getElementById('lat');
        latID.value = lats;
    var lngID = document.getElementById('lng');
        lngID.value = lngs;
}