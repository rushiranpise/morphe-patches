-----------------------------
-----------------------------
--			   --
--	EDITOR SCHEMA	   --
--	Apr 20 2017	   --
--			   --
-----------------------------
-----------------------------

-----------------------
-- Params/Color File --
-----------------------
-- A set of parameters for structure file. Mainly colors, but might define many other
-- things. This file does not have any pre-defined structure, only parameters defined 
-- under names structure fuke expects them to be found.

-- Local 'palette' (constants) for convinience
local Palette = {

	base_default = rgb(0x0095FF),
	black = rgb(0x000000),
	white = rgb(0xFFFFFF),
	red = rgb(0xFF0000),

	map_background = rgb(0xF2F3EE),
	map_missing = rgb(0xEFEFEF),

	labels = rgb(0x3B3838),
	labels_strong = rgb(0x585858),
	labels_bgcolor = rgba(0xFFFFFFBF),

	oneWay = rgba(0x55555566),
	points = rgb(0xB1BEDC),
	selection = rgb(0x1DD1FF),

	-- Roads ------------------------

freeways = rgb(0xa0ff00),
primary = rgb(0xffd833),
secondary = rgb(0xf9fa46),
highways = rgb(0xffae00),
street = rgb(0xd6d6d6),
private = rgb(0xD1CE98 ),
trails4x4 = rgb(0xc4c0b6),
alleys = rgb(0x999489),

	-- Ramps & Exit ------------------------

	ramps = rgb(0xB3BDB5),

	-- Parking ----------------------------

	parking = rgb(0xDDDDDD),
	parking_lots = rgb(0x8B9DC3),
	parking_lots_pins = rgb(0xDEE1EC),

	-- No-Auto ------------------------

	railroads = rgb(0x889999),
	ferry_stroke = rgb(0x800000),
	Runways = rgb(0xFFF68F),

	pedestrian = rgb(0xDFDFDF),
	trails = rgb(0xD0CCC4),
	walkway = rgb(0xDFDFDF),

	-- Areas ------------------------

	cities = rgb(0xFF0000),
	stations = rgb(0xEAEAEA),
	parks = rgb(0xB8D0B0),
	sea = rgb(0xB3CAEC),
	lakes = rgb(0xABC5EA),
	rivers = rgb(0xBBD0EE),

	label_station = rgb(0x817A7A),
	label_cities = rgb(0x3F3F3F),
	label_vegetation = rgb(0x7D9B4D),
	label_water = rgb(0x6383A0),

	-- Others --------------------------

	navigation = rgb(0x001CF0),
	stop_point = rgb(0xA200FF),
	shared = rgb(0xF49B00),
	shared_stop = rgb(0xF47200),
	snail = rgb(0xCECECE),
	snail_via = rgb(0xCECECE),
	snail_share = rgb(0xCECECE),
	snail_via_share = rgb(0xCECECE),
	recording = rgb(0xFF0000),
	recorded = rgb(0x0000FF),

	navigation_stroke = rgb(0x0016c0),
	stop_point_stroke = rgb(0x8100cc),
	shared_stroke = rgb(0xc37c00),
	shared_stop_stroke = rgb(0xc35b00),
	recording_stroke = rgb(0xCC0000),
	recorded_stroke = rgb(0x0000cc),

	alt_color_1 = rgb(0x00CF8A),
	alt_color_2 = rgb(0x1DC4FF),
	alt_color_3 = rgb(0xC160EB),
	alt_color_current = rgb(0x9C86FF),
	alt_color_ride = rgb(0x00C9F9),
	alt_color_direct = rgb(0x8AA7A7),
	
	carpool_ride_fill = rgb(0x00B6EA),
	carpool_ride_stroke = rgb(0x0089B0),
	carpool_direct_fill = rgb(0xD0E3E9),
	carpool_direct_stroke = rgb(0xB1CFD6),
	
	traffic_route_light = rgb(0xFF8736),
	traffic_route_moderate = rgb(0xE55E04),
	traffic_route_heavy = rgb(0xDB1B1B),
	traffic_route_standstill = rgb(0x721716),
	traffic_route_closed_road = rgb(0xA42D2B),

	traffic_arrow_light = rgb(0xFFFFFF),
	traffic_arrow_moderate = rgb(0xFFFFFF),
	traffic_arrow_heavy = rgb(0xFFFFFF),
	traffic_arrow_standstill = rgb(0xFFFFFF),
	traffic_arrow_closed_road = rgb(0xFFFFFF),

}

-- General params --------------------------------

Params = {
	Defaults = {
		font_size = 12,
		declutter_max = 2147483647,
	},
		
	--Example
	Street = {
		font_size = 17,
		declutter = 120,
	}
}

-- Helper functions -------------------------------------

--Transform color for off-navigation rout color
local function traffic_offroute_color(orig_color)
	return color.desaturate(0.5,color.lighten(0.3,orig_color))
end

-- Colors -------------------------------------

Colors = {
	General = {
		global_color_mod = rgba(0xffffff40),
		map_background = Palette.map_background,
		missing = Palette.map_missing,
		labels_bgcolor = Palette.labels_bgcolor,

		map_selection_color = Palette.selection,
		map_one_way_color = Palette.oneWay,
	map_points_color = Palette.points,

		-- Alt colors: { ALT_COLOR_1, ALT_COLOR_2, ALT_COLOR_3, CURRENT_ROUTE_COLOR }
		nav_alt_colors = { Palette.alt_color_1, Palette.alt_color_2, Palette.alt_color_3, Palette.alt_color_current },

		-- Traffic types:{LIGHT, MODERATE, HEAVY, STAND_STILL, UNUSED, CLOSED_ROAD}
		traffic_nav_route_colors = { Palette.traffic_route_light, Palette.traffic_route_moderate, Palette.traffic_route_heavy, Palette.traffic_route_standstill, rgb(0xE12D2D), Palette.traffic_route_closed_road },
		traffic_nav_arrow_colors = { Palette.traffic_arrow_light, Palette.traffic_arrow_moderate, Palette.traffic_arrow_heavy, Palette.traffic_arrow_standstill, rgb(0xbb0b0b), Palette.traffic_arrow_closed_road },

		traffic_route_colors = { 	traffic_offroute_color( Palette.traffic_route_light		), 
									traffic_offroute_color( Palette.traffic_route_moderate	), 
									traffic_offroute_color( Palette.traffic_route_heavy		), 
									traffic_offroute_color( Palette.traffic_route_standstill),
									traffic_offroute_color( rgb(0xE12D2D)					), 
									traffic_offroute_color( Palette.traffic_route_closed_road) };

		traffic_arrow_colors = { 	traffic_offroute_color( Palette.traffic_arrow_light		), 
									traffic_offroute_color( Palette.traffic_arrow_moderate	), 
									traffic_offroute_color( Palette.traffic_arrow_heavy		), 
									traffic_offroute_color( Palette.traffic_arrow_standstill), 
									traffic_offroute_color( rgb(0xbb0b0b)					), 
									traffic_offroute_color( Palette.traffic_arrow_closed_road) },	},

	Defaults = {
		fill = Palette.base_default,
		strokes = Palette.black,
		labels = Palette.labels,
		labels_bgcolor = Palette.labels_bgcolor,
	},
	
	-- Roads -------------------------------------

	Freeways = {
		light_stroke = color.lighten(0.2,Palette.freeways),
		light_fill = color.lighten(0.15,Palette.freeways),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = color.darken(0.1,Palette.freeways),
		medium_fill = Palette.freeways,
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.3,Palette.freeways),
		strong_fill = Palette.freeways,
		strong_label = Palette.labels_strong,
	},

	Primary = {
		light_stroke = color.lighten(0.2,Palette.primary),
		light_fill = color.lighten(0.1,Palette.primary),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = color.darken(0.1,Palette.primary),
		medium_fill = Palette.primary,
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.3,Palette.primary),
		strong_fill = Palette.primary,
		strong_label = Palette.labels_strong,
	},

	Secondary = {
		light_stroke = color.lighten(0.2,Palette.secondary),
		light_fill = color.desaturate(0.01,color.lighten(0.07,Palette.secondary)),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = color.darken(0.1,Palette.secondary),
		medium_fill = Palette.secondary,
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.3,Palette.secondary),
		strong_fill = Palette.secondary,
		strong_label = Palette.labels_strong,
	},

	Highways = {
		light_stroke = color.lighten(0.2,Palette.highways),
		light_fill = color.lighten(0.15,Palette.highways),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = color.darken(0.1,Palette.highways),
		medium_fill = Palette.highways,
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.3,Palette.highways),
		strong_fill = Palette.highways,
		strong_label = Palette.labels_strong,
	},

	Street = {
		light_stroke = color.lighten(0.2,Palette.street),
		light_fill = color.lighten(0.1,Palette.street),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = color.darken(0.1,Palette.street),
		medium_fill = Palette.street,
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.2,Palette.street),
		strong_fill = Palette.street,
		strong_label = Palette.labels_strong,
	},

	Private = {
		light_stroke = color.lighten(0.2,Palette.private),
		light_fill = color.lighten(0.1,Palette.private),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = color.darken(0.1,Palette.private),
		medium_fill = color.darken(0.05,Palette.private),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.2,Palette.private),
		strong_fill = Palette.private,
		strong_label = Palette.labels_strong,
	},

	Trails4X4 = {
		light_stroke = color.lighten(0.2,Palette.trails4x4),
		light_fill = color.lighten(0.2,Palette.trails4x4),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.trails4x4,
		medium_fill = color.lighten(0.05,Palette.trails4x4),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.2,Palette.trails4x4),
		strong_fill = Palette.trails4x4,
		strong_label = Palette.labels_strong,
	},

	--Ramps and Exits-------------------------------------

	Ramps = {
		light_stroke = color.lighten(0.2,Palette.ramps),
		light_fill = Palette.ramps,

		medium_stroke = color.darken(0.1,Palette.ramps),
		medium_fill = color.lighten(0.02,Palette.ramps),

		strong_stroke = color.darken(0.2,Palette.ramps),
		strong_fill = Palette.ramps,
	},

	Exit = {
		light_stroke = color.lighten(0.2,Palette.ramps),
		light_fill = Palette.ramps,

		medium_stroke = color.darken(0.1,Palette.ramps),
		medium_fill = color.lighten(0.02,Palette.ramps),

		strong_stroke = color.darken(0.2,Palette.ramps),
		strong_fill = Palette.ramps,
	},

	--Parking-------------------------------------

	Parking = {
		light_stroke = color.lighten(0.2,Palette.parking),
		light_fill = color.lighten(0.1,Palette.parking),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = color.darken(0.1,Palette.parking),
		medium_fill = color.darken(0.05,Palette.parking),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.2,Palette.parking),
		strong_fill = Palette.parking,
		strong_label = Palette.labels_strong,
	},

	ParkingLots = {
		light_fill = color.saturate(0.07,color.lighten(0.05,Palette.parking_lots)),
		strong_fill = Palette.parking_lots,		
	},

	ParkingLotsPins = {
		light_fill = color.saturate(0.07,color.lighten(0.05,Palette.parking_lots_pins)),
		strong_fill = Palette.parking_lots_pins,	
	},

	--Alleys-------------------------------------

	Alleys = {
		light_stroke = Palette.map_background,
		light_fill = color.lighten(0.1,Palette.alleys),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.map_background,
		medium_fill = color.shiftHue(-0.05,color.lighten(0.05,Palette.alleys)),
		medium_label = Palette.labels,

		strong_stroke = Palette.map_background,
		strong_fill = Palette.alleys,
		strong_label = Palette.labels_strong,
	},

	--No-Auto-------------------------------------

	Railroads = {
		light_stroke = color.lighten(0.13,Palette.railroads),
		light_fill = Palette.railroads,

		medium_stroke = color.lighten(0.07,Palette.railroads),
		medium_fill = Palette.railroads,

		strong_stroke = Palette.railroads,
		strong_fill = Palette.railroads,

		texture = "rail_pattern",
		-- shadow = Palette.base_default,
	},

	Ferry = {
		light_stroke = Palette.ferry_stroke,
		light_label = color.darken(0.05,Palette.label_water),

		medium_stroke = Palette.ferry_stroke,
		medium_label = color.darken(0.07,Palette.label_water),

		strong_stroke = Palette.ferry_stroke,
		strong_label = color.darken(0.1,Palette.label_water),

		texture = "longdash_pattern",
	},

	Runways = {
		light_stroke = Palette.Runways,

		medium_stroke = Palette.Runways,

		strong_stroke = Palette.Runways,

		texture = longdash_pattern,
	},

	Pedestrian = {
		light_stroke = color.lighten(0.2,Palette.pedestrian),
		light_fill = color.lighten(0.07,Palette.pedestrian),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.pedestrian,
		medium_fill = color.lighten(0.05,Palette.pedestrian),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.2,Palette.pedestrian),
		strong_fill = Palette.pedestrian,
		strong_label = Palette.labels_strong,
	},

	Trails = {
		light_stroke = color.lighten(0.2,Palette.trails),
		light_fill = color.lighten(0.07,Palette.trails),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.trails,
		medium_fill = color.lighten(0.05,Palette.trails),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.2,Palette.trails),
		strong_fill = Palette.trails,
		strong_label = Palette.labels_strong,
	},

	Walkway = {
		light_stroke = color.lighten(0.2,Palette.walkway),
		light_fill = color.lighten(0.07,Palette.walkway),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.walkway,
		medium_fill = color.lighten(0.05,Palette.walkway),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.2,Palette.walkway),
		strong_fill = Palette.walkway,
		strong_label = Palette.labels_strong,
	},

	-- Areas -------------------------------------

	Cities = {
		light_fill = Palette.cities,
		light_label = color.saturate(0.01,color.lighten(0.25,Palette.label_cities)),

		medium_label = color.lighten(0.25,Palette.label_cities),

		strong_fill = Palette.cities,
		strong_label = Palette.label_cities,
	},

	Stations = {
		light_fill = color.darken(0.02,Palette.stations),
		
		strong_fill = Palette.stations,
		strong_label = Palette.label_station,
	},

	Parks = {
		strong_fill = Palette.parks,
		strong_label = Palette.label_vegetation,	
	},

	Rivers = {
		strong_fill = Palette.rivers,
		strong_label = Palette.label_water,	
	},

	Lakes = {
		strong_fill = Palette.lakes,
		strong_label = Palette.label_water,	
	},

	Sea = {
		strong_fill = Palette.sea,
		strong_label = Palette.label_water,
	},

	-- Navigation -------------------------------------

	Navigation = {
		fill = Palette.navigation,
		stroke = Palette.navigation_stroke,
		--label = Palette.white,
		--labelBg = rgba(0xA200FF50),
	},
	
	StopPoint = {
		fill = Palette.stop_point,
		stroke = Palette.stop_point_stroke,
		--label = Palette.white,
		--labelBg = rgba(0x5500F050),
	},

	Shared = {
		fill = Palette.shared,
		stroke = Palette.shared_stroke,
		--label = Palette.white,
		--labelBg = rgba(0x09C26450),
	},

	SharedStop = {
		fill = Palette.shared_stop,
		stroke = Palette.shared_stop_stroke,
		--label = Palette.white,
		--labelBg = rgba(0x09A1C250),
	},
	
	RouteSnail = {
		fill = Palette.snail,
		stroke = Palette.navigation_stroke,
	},
	
	ViaSnail = {
		fill = Palette.snail_via,
		stroke = Palette.stop_point_stroke,
	},
	
	SharedSnail = {
		fill = Palette.snail_share,
		stroke = Palette.shared_stroke,
	},
	
	SharedViaSnail = {
		fill = Palette.snail_via_share,
		stroke = Palette.shared_stop_stroke,
	},
	
	Recording = {
		fill = Palette.recording,
		stroke = Palette.recording_stroke,
	},
	
	Recorded = {
		fill = Palette.recorded,
		stroke = Palette.recorded_stroke,
	},
	
	CarpoolRide = {
		fill = Palette.carpool_ride_fill,
		stroke = Palette.carpool_ride_stroke,
	},
	
	CarpoolDirect = {
		fill = Palette.carpool_direct_fill,
		stroke = Palette.carpool_direct_stroke,
	},
}
