set smooth [ list xx ex go in ba vb ho vh im ]

set col(xx) "#808080"
set col(ex) "#00e090"
set col(go) "#00ff00"
set col(in) "#d0f000"
set col(ba) "#f07000"
set col(vb) "#e00030"
set col(ho) "#d000d0"
set col(vh) "#900090"
set col(im) "#300030"

set surface [ list ispavd asphal concre paving cobble wooden metall grapav compac gravel pebble grasss ground sandig unpavd iceroa unknow ]

set key(ispavd) "paved"
set key(asphal) "asphalt"
set key(concre) "concrete"
set key(paving) "paving_stones"
set key(cobble) "cobblestone"
set key(wooden) "wood|wooden"
set key(metall) "metal"
set key(grapav) "grass_paver"
set key(compac) "compacted"
set key(gravel) "gravel"
set key(pebble) "pebblestone"
set key(grasss) "grass"
set key(ground) "ground|earth|mud|dirt"
set key(sandig) "sand"
set key(unpavd) "unpaved"
set key(iceroa) "ice_road"
set key(unknow) "~"

set fi [ open "surfacepatterns.tmpl" "r" ]
set pa [ read $fi ]
close $fi

set fi [ open "surfacerules.tmpl" "r" ]
set ru [ read $fi ]
close $fi

set fo [ open "surfacepatterns-z17.xml" "w" ]

puts $fo "
<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:svg=\"http://www.w3.org/2000/svg\">
<!-- DON'T EDIT, THE FILE IS GENERATED BY SCRIPT surfaces.tcl -->"

foreach s $smooth {
   set c $col($s)
   eval "puts $fo \"$pa\""
}

puts $fo "</svg>" 

close $fo

set fo [ open "surfacestyle-z17.xml" "w" ]

puts $fo "
.surface-area \{ stroke: none; \}
.surface-line \{ fill:   none; stroke-width: 1.0px; \}"

foreach f $surface {
   foreach s $smooth {
      puts $fo ".$f-line-$s \{ stroke: url(#$f-pattern-$s); \}"
      puts $fo ".$f-area-$s \{ fill:   url(#$f-pattern-$s); \}"
   }
}
close $fo

set fo [ open "surfacerules-z17.xml" "w" ]

foreach k $surface {
   set v $key($k)
   eval "puts $fo \"$ru\""
}

close $fo
