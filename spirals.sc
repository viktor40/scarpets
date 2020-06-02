__command() -> null;

// to store marker postiions and object handles
global_settings = m(
						l( 'show_pos' , true ),
						l( 'paste_with_air' , false ),
						l( 'axis' , 'y' ),
						l( 'replace_block' , false ),
						l( 'rotate' , false ), //TODO
						l( 'slope_mode', false ),
						l( 'max_template_size', 100 ),
						l( 'preview_enabled', false ) //TODO
					);
global_block_alias = m(
						l( 'water_bucket', 'water' ),
						l( 'lava_bucket', 'lava'),
						l( 'feather', 'air'),
						l( 'ender_eye', 'end_portal'),
						l( 'flint_and_steel', 'nether_portal')
					);


////// Settings'n stuff ///////

set_max_template_size(value) -> (
	if( type(value) == 'number' && value > 0, 
		global_settings:'max_template_size' = value;
		print(format(str('b Max tempalte size value set to %s', value) ) ),
		print(format('rb Error: ', 'y Max template size should be a positive number') )
	);
	return('')
);

set_axis(axis) -> (
	if( ( l('x','y','z')~axis ) == null, 
		print(format('rb Error: ', 'y Axis must be one of ', 'yb x, y ', 'y or ', 'yb z'));
		return('')
	);
	global_settings:'axis' = axis;
	if( axis == 'x',
		__get_step(circle, perimeter, advance_step, i) ->(
			circle_pos = circle:(i%perimeter);
			step = l(i * advance_step, circle_pos:0, circle_pos:1) ;
		),
		axis == 'y',
		__get_step(circle, perimeter, advance_step, i) ->( //defaults to axis y
			circle_pos = circle:(i%perimeter);
			step = l(circle_pos:0, i * advance_step, circle_pos:1)
		),
		axis == 'z',
		__get_step(circle, perimeter, advance_step, i) ->(
			circle_pos = circle:(i%perimeter);
			step = l(circle_pos:0, circle_pos:1, i * advance_step) ;
		),
	);
	print(format(str('b Spirals will now generate along the %s axis', axis) ) );
	return('')
);

toggle_paste_with_air() -> (
	global_settings:'paste_with_air' = !global_settings:'paste_with_air';
	if(global_settings:'paste_with_air',
		print('Template will now be pasted with air'),
		print('Template will now be pasted without air')
	);
	return('')
);

toggle_replace_block() -> (
	global_settings:'replace_block' = !global_settings:'replace_block';
	if(global_settings:'replace_block',
		//if replace blocks
		print( format('b Spiral will now only replce block in your offhand.\n',
			'g Hold bucket for liquids, feather for air, ender eye for end portal, and flint and steel for nether portal.') );
		__set_block(pos, material, replace_block) -> if(block(pos) == replace_block, set(pos, material) ),
		//else
		print(format( 'b Spiral will paste completly, replacing whatever is there.') );
		__set_block(pos, material, replace_block) -> set(pos , material)
	);
	return('')
);

toggle_slope_mode() -> (
	global_settings:'slope_mode' = !global_settings:'slope_mode';
	if(global_settings:'slope_mode',
		print(format('b Second argument of spiral commands is now slope (in blocks)') ),
		print(format('b Second argument of spiral commands is now pitch(separation between revolutions)') )
	);
	return('')
);

// generate interactive string for togglable parameter
__make_toggle_setting(parameter, hover) -> (
	str_list = l(
		str('w * %s: ', parameter), 
		str('^y %s', hover),
	);
	str_list = extend(str_list, __get_button('true', parameter) );
	str_list = extend(str_list, __get_button('false', parameter) );
	print(format(str_list))
);

__get_active_button(value) -> (
	l( str('yb [%s] ', value) )
);

__get_inactive_button(value, parameter) -> (
	l( 
		str('g [%s] ', value),
		str('^gb Click to toggle'),
		str('!/spirals toggle_%s', parameter)
	)
);

__get_button(value, parameter) -> (
	bool_val = if(bool(value), global_settings:parameter, !global_settings:parameter);
	if( bool_val, __get_active_button(value), __get_inactive_button(value, parameter) )
);

// generate interactive string for parameter with options
__make_value_setting(parameter, hover, options) -> (
	str_list = l(
		str('w * %s: ', parameter), 
		str('^y %s', hover),
	);
	options_list = l();
	map( options, 
			len = length(options_list);
			options_list:len = str('%sb [%s]', if(global_settings:parameter == _, 'y', 'g',), _);
			options_list:(len+1) = '^bg Click to set this value';
			options_list:(len+2) = str('?/spirals set_%s %s', parameter, _) 
	);
	print(format( extend(str_list, options_list) ))
);

// print all settings
settings() -> (
	print(format( 'b Spirals app settings:' ));
	__make_toggle_setting('show_pos', 'Shows markers and outlines selection');
	__make_toggle_setting('paste_with_air', 'Includes air when pasting template');
	__make_toggle_setting('replace_block', 'Spirals will only be generated replacing block in offhand');
	__make_toggle_setting('slope_mode', 'Defines behaviour of second argument in spiral definitions: slope or pitch');
	__make_value_setting('axis', 'Axis along which spirals are generated', l('x', 'y', 'z') );
	__make_value_setting('max_template_size', 'Limits template size to avoid freezing the game if you mess up the selection', l(20, 100, 1200) );
	return('')
);


////// Utilities ///////

__get_replace_block() -> (
	block = query(player(), 'holds', 'offhand'):0;
	alias = global_block_alias:block;
	if(alias==null, block, alias)
);

__get_step(circle, perimeter, advance_step, i) ->( //defaults to axis y
	circle_pos = circle:(i%perimeter);
	step = l(circle_pos:0, i * advance_step, circle_pos:1) ;
);

__set_block(pos, material, replace_block) -> ( //defaults to no replace
	set(pos , material) 
);


__rotated90(list_to_rotate) -> ( //rotates 90 degrees
	map(list_to_rotate, l(_:1, -_:0))
);

extend(list, extension) -> (
	len = length(list);
	for(extension, list:(len+_i) = _);
	return(list)
);

__make_circle(radius) -> (
	z_function(x, outer(radius)) -> round(sqrt(radius * radius - x*x));
	range_val = radius * cos(45); //this spans a quarter circle
	x_range = range(-range_val, range_val);
	
	quarter1 = map(x_range, l(_, z_function(_)) ); // starts with quarter circle
	half = extend(quarter1, __rotated90(quarter1)); // add a quarter by rotating it 90 degrees
	extend(half,__rotated90(__rotated90(half))); // rotate the half circle 180 degrees and add it
);

// mainly for debug porpuses
_circle(radius, material) -> (
	circ = __make_circle(radius);
	c = pos(player());
	for(circ, 
		set(c + l(_:0, 0, _:1), material); 
		create_marker(str(_i), c + l(_:0, 0, _:1))
	);	
);

__get_center() -> (
	if(global_positions:2 == null,
		pos(player()),
		global_positions:2
	)
);


////// Material spirals ///////

//main funtion todraw spiral from material
__draw_spiral(circle, center, pitch, size, material) -> (
	l(cx, cy, cz) = center; // center coordiantes
	perimeter = length(circle); // ammount of blocks in one revolution
	
	replace_block = __get_replace_block();
	advance_step = if(global_settings:'slope_mode', pitch, pitch/perimeter); //pitch encodes slope if slope_mode == true
	
	loop(floor( size / advance_step), //loop over the total ammount of spirals
		this_step =  __get_step(circle, perimeter, advance_step, _);
		__set_block(center + this_step , material, replace_block);
	);
);

spiral(radius, pitch, size, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	__draw_spiral(circle, center, pitch, size, material);
);

antispiral(radius, pitch, size, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = map(range(length(circle)-1, -1, -1), circle:_); // to spin the other way around
	__draw_spiral(circle, center, pitch, size, material);

);

multispiral(radius, pitch, size, ammount, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = extend(slice(circle, jump), slice(circle, 0, jump) ); // redefine the circle last for this iteration
		__draw_spiral(this_circ, center, pitch, size, material);
	);
);

antimultispiral(radius, pitch, size, ammount, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = map(range(length(circle)-1, -1, -1), circle:_); // to spin the other way around
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = extend(slice(circle, jump), slice(circle, 0, jump) ); // redefine the circle last for this iteration
		__draw_spiral(this_circ, center, pitch, size, material);
	);
);



////// Template spirals ///////

// saves selected area, minus air
__make_template() -> (
	global_template = l();
	origin = map(range(3), min(global_positions:0:_, global_positions:1:_)); //negative-most corner in all dimensions
	volume(
		global_positions:0:0, global_positions:0:1, global_positions:0:2,
		global_positions:1:0, global_positions:1:1, global_positions:1:2,
		if(global_settings:'paste_with_air',
			global_template:length(global_template) = l(pos(_)-origin, _),
			if(!air(_), global_template:length(global_template) = l(pos(_)-origin, _) ) //save non-air blocks and positions
		);
	);
	// Handle template size warning
	if(length(global_template) > global_settings:'max_template_size',
		print( format(
			'buy Warning',
			'y : ',
			'w Template is too big. Your tried to paste ', 
			str('by %d ', length(global_template)),
			str('w blocks %s, but max size is ', if(global_settings:'paste_with_air', '(counting air)', '(not counting air)') ),
			str('by %d', global_settings:'max_template_size' ),
			'w .\nTry increasing it with ',
			'b [this] ', '^t Click here!', '?/spirals set_max_template_size 200',
			'w command.'
		) );
		true, // tempalte too big, don't paste it
		false // paste it
	)
);

// clone template at given position
__clone_template(pos, replace_block) -> (
	for(global_template, __set_block(pos + _:0, _:1, replace_block) );
);

// main function to draw spirals from template (selected area)
__draw_spiral_from_template(circle, center, pitch, size) -> (
	perimeter = length(circle); // ammount of blocks in one revolution
	
	if(__make_template(), return() ); //tempalte was too big
	offset = map(global_positions:0 - global_positions:1, abs(_)); //offsets the selection so that it clones it in the center of the block
	advance_step = if(global_settings:'slope_mode', pitch, pitch/perimeter); //pitch encodes slope if slope_mode == true
	
	replace_block = __get_replace_block();
	
	loop(floor( size / advance_step), //loop over the total ammount of spirals
		this_step =  __get_step(circle, perimeter, advance_step, _);
		__clone_template(center + this_step, replace_block);
	);
);

spiral_template(radius, pitch, size) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	__draw_spiral_from_template(circle, center, pitch, size);
);

antispiral_template(radius, pitch, size) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = map(range(length(circle)-1, -1, -1), circle:_); // to spin the other way around
	__draw_spiral_from_template(circle, center, pitch, size);
);

multispiral_template(radius, pitch, size, ammount) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = extend(slice(circle, jump), slice(circle, 0, jump) );
		__draw_spiral_from_template(this_circ, center, pitch, size);
	);
);

antimultispiral_template(radius, pitch, size, ammount) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = map(range(length(circle)-1, -1, -1), circle:_); // to spin the other way around
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = extend(slice(circle, jump), slice(circle, 0, jump) );
		__draw_spiral_from_template(this_circ, center, pitch, size);
	);
);

////// Handle Markers //////

// Spawn a marker
__mark(i, position) -> (
 	colours = l('red', 'lime', 'light_blue'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete'); // crete the marker
	run(str( //modify some stuff to make it fancier
		'data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid') 
		));
	put(global_armor_stands, i-1, query(e, 'id')); //save the id for future use
);

__remove_mark(i) -> (
	e = entity_id(global_armor_stands:(i));
 	if(e != null, modify(e, 'remove'));
);

// set a position
set_pos(i) -> (
	try( // position index must be 1, 2 or 3 
 		if( !reduce(range(1,4), _a + (_==i), 0),
			throw();
		),
		print(format('rb Error: ', 'y Input must be either 1, 2 or 3 for position to set. You input ' + i) );
		return()
	);
	// position to be set at the block the player is aiming at, or player position, if there is none
	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:(i-1) = tha_pos; // save to global positions
	if(all(slice(global_positions, 0, 2), _!=null), global_all_set = true); 
	
	print(str('Set your position %d to ',i) + tha_pos);

	if(global_settings:'show_pos', // remove previous marker for set positi, if aplicable
		__remove_mark(i-1); //-1 because stupid indexes
		__mark(i, tha_pos);
	);

);

// print list of positions
get_pos() -> (
	for(global_positions, 
 		print(str('Position %d is %s', 
				_i+1, if(_==null, 'not set', _)));
 	)
);

// toggle markers and bounding box visibility
toggle_show_pos() ->(
	global_settings:'show_pos' = !global_settings:'show_pos'; 
	if(global_settings:'show_pos',
		( // summon the markers
			for(global_positions, 
				if(_!=null, __mark( (_i+1) , _) );
			);
			print('Positions shown');
		),
		// else
		( //remove the markers
			for(global_armor_stands, 
				__remove_mark(_i);
			);
			print('Positions hidden');
		);
	);
);

// remove all markers
reset_positions() -> (
	loop(3, 
		__remove_mark(_);
	);
	global_positions = l(null, null, null); // TODO: player-specific positions
	global_all_set = false;
	global_armor_stands = l(null, null, null);
);
reset_positions();

// set position 1 if player left clicks with a golden sword
__on_player_clicks_block(player, block, face) -> (
	if(query(player(), 'holds'):0 == 'golden_sword',
		set_pos(1);
	);
);

// set position 2 if player right clicks with a golden sword
__on_player_uses_item(player, item_tuple, hand) -> (
	if(query(player(), 'holds'):0 == 'golden_sword',
		if(query(player(), 'sneaking'),
			set_pos(3),
			set_pos(2)
		);
	);
);

// display particle cube once per second to select marked volume
__on_tick() -> (

	if( global_all_set && global_settings:'show_pos' &&	tick_time()%20 == 0,			
		min_pos = map(range(3), min(global_positions:0:_, global_positions:1:_));
		max_pos = map(range(3), max(global_positions:0:_, global_positions:1:_));
		particle_rect('end_rod', min_pos, max_pos + l(1, 1, 1))
	);
);

__on_tick_ender() -> (

	if( global_all_set && global_settings:'show_pos' &&	tick_time()%20 == 0,			
		min_pos = map(range(3), min(global_positions:0:_, global_positions:1:_));
		max_pos = map(range(3), max(global_positions:0:_, global_positions:1:_));
		particle_rect('end_rod', min_pos, max_pos + l(1, 1, 1))
	);
);

__on_tick_nether() -> (

	if( global_all_set && global_settings:'show_pos' &&	tick_time()%20 == 0,			
		min_pos = map(range(3), min(global_positions:0:_, global_positions:1:_));
		max_pos = map(range(3), max(global_positions:0:_, global_positions:1:_));
		particle_rect('end_rod', min_pos, max_pos + l(1, 1, 1))
	);
);