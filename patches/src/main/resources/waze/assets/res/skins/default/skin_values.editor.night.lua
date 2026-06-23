-----------------------------
-----------------------------
-- EDITOR LIGHT NIGHT SCHEMA -- 
--    ETIQUETTE BLANCHE    --
--      22 SEP 2023        -- 
--       ALEX02-GTT        -- 
--      Thx chuppito       --
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

	map_background = rgb(0x000000),
	map_missing = rgb(0x000000),

	labels = rgb(0xa9a9a9),
	labels_strong = rgb(0xb0b0b0),
	labels_bgcolor = rgba(0x1f1f1fbf),

	oneWay = rgba(0xFFFFFF55),
	points = rgb(0xB2BFDC),
	selection = rgb(0x88c1e3),

	-- Roads ------------------------

	freeways = rgb(0xC577D2),
	primary = rgb(0x42A1BD),
	secondary = rgb(0x69C288),
	highways = rgb(0xD6D14F),
	street = rgb(0xCCCBBA),
	private = rgb(0xB7BB80),
	trails4x4 = rgb(0x665A37),
  alleys = rgb(0xCCCBBA),

	-- Ramps & Exit ------------------------

	ramps = rgb(0xB3BDB5),

	-- Parking ----------------------------

	parking = rgb(0xAAAAAA),
	parking_lots = rgb(0x0000AB),
	parking_lots_pins = rgb(0x517BBA),

	-- No-Auto ------------------------

	railroads = rgb(0x647070),
	ferry_stroke = rgb(0x6993b4),

	pedestrian = rgb(0x353535),
	trails = rgb(0x353535),
	walkway = rgb(0x353535),

	-- Areas ------------------------

	cities = rgb(0xFF0000),
	stations = rgb(0x464c52),
	parks = rgb(0x647c3e),
	sea = rgb(0x3689ac),
	lakes = rgb(0x598F91),
	rivers = rgb(0x598F91),

	label_station = rgb(0xB5ABAB),
	label_cities = rgb(0xdddddd),
	label_vegetation = rgb(0x7d9b4d),
	label_water = rgb(0x6383a0),

	-- Others --------------------------

	navigation = rgb(0x07E4FF),
	stop_point = rgb(0x07E4FF),
	shared = rgb(0x07E4FF),
	shared_stop = rgb(0x07E4FF),
  snail = rgb(0xCECECE),
  snail_via = rgb(0xCECECE),
  snail_share = rgb(0xCECECE),
  snail_via_share = rgb(0xCECECE),
  recording = rgb(0xFF0000),
  recorded = rgb(0x0000FF),
  detour = rgb(0x25ce09),
  detour_stroke = rgb(0x25ce09),

	alt_color_1 = rgb(0x3dbce0),
	alt_color_2 = rgb(0x71c113),
	alt_color_3 = rgb(0xc01cac),
	alt_color_current = rgb(0x07e4ff),
  alt_color_ride = rgb(0x00C9F9),
  alt_color_direct = rgb(0x8AA7A7),

  carpool_ride_fill = rgb(0x00B6EA),
  carpool_ride_stroke = rgb(0x0089B0),
  carpool_direct_fill = rgb(0xD0E3E9),
  carpool_direct_stroke = rgb(0xB1CFD6),

  route_preview_unselected_fill = rgb(0x25ce09),
  route_preview_unselected_stroke = rgb(0x25ce09),

	traffic_route_light = rgb(0xc1aa43),
	traffic_route_moderate = rgb(0xb85122),
	traffic_route_heavy = rgb(0xa51e1e),
	traffic_route_standstill = rgb(0x7b2424),	
	--traffic_route_standstill = 0,			
	traffic_route_closed_road = rgb(0xA42D2B),	

	traffic_arrow_light = rgb(0xFFFFFF),
	traffic_arrow_moderate = rgb(0xFFFFFF),
	traffic_arrow_heavy = rgb(0xFFFFFF),
	traffic_arrow_standstill = rgb(0xFFFFFF),
	--traffic_arrow_standstill = 0,			
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
	return color.desaturate(0.2,color.darken(0.6,orig_color))
end

--Transform traffic color for showing on an unselected-route
local function traffic_unselected_route_color(orig_color)
	return color.desaturate(0.17,color.darken(0.05,orig_color))
end

-- Colors -------------------------------------

Colors = {
	General = {
		global_color_mod = rgba(0x555a6060),

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
									traffic_offroute_color( Palette.traffic_route_closed_road) },

		traffic_arrow_colors = { 	traffic_offroute_color( Palette.traffic_arrow_light		), 
									traffic_offroute_color( Palette.traffic_arrow_moderate	), 
									traffic_offroute_color( Palette.traffic_arrow_heavy		), 
									traffic_offroute_color( Palette.traffic_arrow_standstill), 
									traffic_offroute_color( rgb(0xbb0b0b)					), 
									traffic_offroute_color( Palette.traffic_arrow_closed_road) },

		traffic_unselected_route_colors = {   traffic_unselected_route_color( Palette.traffic_route_light    ),
									traffic_unselected_route_color( Palette.traffic_route_moderate  ),
									traffic_unselected_route_color( Palette.traffic_route_heavy    ),
									traffic_unselected_route_color( Palette.traffic_route_standstill),
									traffic_unselected_route_color( rgb(0xE12D2D)          ),
									traffic_unselected_route_color( Palette.traffic_route_closed_road) },
         
	},

	Defaults = {
		fill = Palette.base_default,
		strokes = Palette.black,
		labels = Palette.labels,
		labels_bgcolor = Palette.labels_bgcolor,
	},
	
	-- Roads -------------------------------------

	Freeways = {
		light_stroke = color.lighten(0.1,Palette.freeways),
		light_fill = color.lighten(0.08,Palette.freeways),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.freeways,
		medium_fill = color.lighten(0.05,Palette.freeways),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.1,Palette.freeways),
		strong_fill = Palette.freeways,
		strong_label = Palette.labels_strong,
	},

	Primary = {
		light_stroke = color.lighten(0.15,Palette.primary),
		light_fill = color.lighten(0.06,Palette.primary),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.primary,
		medium_fill = color.lighten(0.04,Palette.primary),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.1,Palette.primary),
		strong_fill = Palette.primary,
		strong_label = Palette.labels_strong,
	},

	Secondary = {
		light_stroke = color.lighten(0.1,Palette.secondary),
		light_fill = color.desaturate(0.01,color.lighten(0.07,Palette.secondary)),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.secondary,
		medium_fill = color.lighten(0.05,Palette.secondary),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.1,Palette.secondary),
		strong_fill = Palette.secondary,
		strong_label = Palette.labels_strong,
	},

	Highways = {
		light_stroke = color.lighten(0.04,Palette.highways),
		light_fill = color.lighten(0.07,Palette.highways),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.highways,
		medium_fill = color.lighten(0.04,Palette.highways),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.1,Palette.highways),
		strong_fill = Palette.highways,
		strong_label = Palette.labels_strong,
	},

	Street = {
		light_stroke = color.darken(0.05,Palette.street),
		light_fill = color.lighten(0.1,Palette.street),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.street,
		medium_fill = color.lighten(0.05,Palette.street),
		medium_label = Palette.labels,

		strong_stroke = Palette.map_background,
		strong_fill = Palette.street,
		strong_label = Palette.labels_strong,
	},

	Private = {
		light_stroke = Palette.map_background,
		light_fill = color.lighten(0.2,Palette.private),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.map_background,
		medium_fill = color.shiftHue(-0.03,color.lighten(0.02,Palette.private)),
		medium_label = Palette.labels,

		strong_stroke = Palette.map_background,
		strong_fill = Palette.private,
		strong_label = Palette.labels_strong,
	},

	Trails4X4 = {
		light_stroke = color.darken(0.1,Palette.trails4x4),
		light_fill = color.lighten(0.2,Palette.trails4x4),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.trails4x4,
		medium_fill = color.lighten(0.05,Palette.trails4x4),
		medium_label = Palette.labels,

		strong_stroke = color.darken(0.1,Palette.trails4x4),
		strong_fill = Palette.trails4x4,
		strong_label = Palette.labels_strong,
	},

	--Ramps and Exits-------------------------------------

	Ramps = {
		light_stroke = Palette.rampsStroke,
		light_fill = Palette.ramps,

		medium_stroke = Palette.ramps,
		medium_fill = color.desaturate(0.1,color.lighten(0.05,Palette.ramps)),

		strong_stroke = color.shiftHue(0.05,color.darken(0.05,Palette.ramps)),
		strong_fill = Palette.ramps,
	},

	Exit = {
		light_stroke = Palette.rampsStroke,
		light_fill = Palette.ramps,

		medium_stroke = Palette.ramps,
		medium_fill = color.shiftHue(-0.2,color.lighten(0.15,Palette.ramps)),

		strong_stroke = color.shiftHue(0.2,color.darken(0.15,Palette.ramps)),
		strong_fill = Palette.ramps,
	},

	--Parking-------------------------------------

	Parking = {
		light_stroke = Palette.map_background,
		light_fill = color.lighten(0.1,Palette.parking),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.map_background,
		medium_fill = color.shiftHue(-0.03,color.lighten(0.02,Palette.parking)),
		medium_label = Palette.labels,

		strong_stroke = Palette.map_background,
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
      medium_fill = color.shiftHue(-0.03,color.lighten(0.02,Palette.alleys)),
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

	Pedestrian = {
		light_stroke = Palette.map_background,
		light_fill = color.lighten(0.07,Palette.pedestrian),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.map_background,
		medium_fill = color.lighten(0.05,Palette.pedestrian),
		medium_label = Palette.labels,

		strong_stroke = Palette.map_background,
		strong_fill = Palette.pedestrian,
		strong_label = Palette.labels_strong,
	},

	Trails = {
		light_stroke = Palette.map_background,
		light_fill = color.lighten(0.07,Palette.trails),
		light_label = color.darken(0.10,Palette.white),

		medium_stroke = Palette.map_background,
		medium_fill = color.lighten(0.05,Palette.trails),
		medium_label = color.darken(0.15,Palette.white),

		strong_stroke = Palette.map_background,
		strong_fill = Palette.trails,
		strong_label = color.darken(0.18,Palette.white),
	},

	Walkway = {
		light_stroke = Palette.map_background,
		light_fill = color.lighten(0.07,Palette.walkway),
		light_label = color.lighten(0.1,Palette.labels),

		medium_stroke = Palette.map_background,
		medium_fill = color.lighten(0.05,Palette.walkway),
		medium_label = Palette.labels,

		strong_stroke = Palette.map_background,
		strong_fill = Palette.walkway,
		strong_label = Palette.labels_strong,
	},

	-- Areas -------------------------------------

	Cities = {
		light_fill = Palette.cities,
		light_label = color.saturate(0.01,color.darken(0.2,Palette.label_cities)),

		medium_label = color.lighten(0.25,Palette.label_cities),

		strong_fill = Palette.cities,
		strong_label = Palette.label_cities,
	},


	Stations = {
		light_fill = color.saturate(0.07,color.lighten(0.05,Palette.stations)),
		
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
		stroke = color.darken(0.3,Palette.navigation),
		--label = Palette.white,
		--labelBg = rgb(0x07E4FF),
	},
	
	StopPoint = {
		fill = Palette.stop_point,
		stroke = color.darken(0.2,Palette.stop_point),
		--label = Palette.white,
		--labelBg = rgb(0x25ce09),
	},

	Shared = {
		fill = Palette.shared,
		stroke = color.darken(0.2,Palette.shared),
		--label = rgb(0xf49b00),
		--labelBg = rgb(0x07E4FF),
	},

	SharedStop = {
		fill = Palette.shared_stop,
		stroke = color.darken(0.2,Palette.shared_stop),
		--label = rgb(0xf49b00),
		--labelBg = rgb(0x25ce09),
	},
   
   RouteSnail = {
      fill = Palette.snail,
      stroke = color.darken(0.2,Palette.snail),
   },
   
   ViaSnail = {
      fill = Palette.snail_via,
      stroke = color.darken(0.2,Palette.snail_via),
   },
   
   SharedSnail = {
      fill = Palette.snail_share,
      stroke = color.darken(0.2,Palette.snail_share),
   },
   
   SharedViaSnail = {
      fill = Palette.snail_via_share,
      stroke = color.darken(0.2,Palette.snail_via_share),
   },
   
   Recording = {
      fill = Palette.recording,
      stroke = color.darken(0.2,Palette.recording),
   },
   
   Recorded = {
      fill = Palette.recorded,
      stroke = color.darken(0.2,Palette.recorded),
   },
   
   CarpoolRide = {
      fill = Palette.carpool_ride_fill,
      stroke = Palette.carpool_ride_stroke,
   },
   
   CarpoolDirect = {
      fill = Palette.carpool_direct_fill,
      stroke = Palette.carpool_direct_stroke,
   },

   RoutePreviewSelected = {
      fill = Palette.navigation,
      stroke = color.darken(0.3,Palette.navigation),
   },

   RoutePreviewUnselected = {
      fill = Palette.route_preview_unselected_fill,
      stroke = Palette.route_preview_unselected_stroke,
   },

   RouteDetour = {
    fill = Palette.detour,
    stroke = Palette.detour_stroke,
    label = Palette.white,
    labelBg = Palette.black,
   },
}
