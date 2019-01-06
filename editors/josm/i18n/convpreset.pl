#! /usr/bin/perl -w

# Written by Dirk Stöcker <openstreetmap@dstoecker.de>
# Public domain, no rights reserved.

use strict;

my $item = "";
my $chunk = "";
my $group;
my $combo_n;
my $combo_type;
my $result = 0;
my $comment = 0;
my $vctx;

# This is a simple conversion and in no way a complete XML parser
# but it works with a default Perl installation

# Print a header to write valid Java code.  No line break,
# so that the input and output line numbers will match.
print "class trans_preset { void tr(String s){} void f() {";

sub fix($)
{
  my ($val) = @_;
  $val =~ s/'/''/g;
  $val =~ s/&lt;/</g;
  $val =~ s/&gt;/>/g;
  $val =~ s/&amp;/&/g;
  return $val;
}

sub infoblock
{
  my $r = "";
  $r .= " item $item" if $item;
  $r .= " chunk $chunk" if $chunk;
  $r .= " group $group" if $group;
  $r .= " combo $combo_type $combo_n" if $combo_type;
  $r .= " $_[0]" if $_[0];
  return $r ? "/* $r */ " : "";
}

my $linenr = 0;
while(my $line = <>)
{
  ++$linenr;
  chomp($line);
  print "tr(\"---DUMMY-MARKER---\"); ";
  if($line =~ /<item\s+name=(".*?")/ || $line =~ /<item.* name=(".*?")/)
  {
    my $val = fix($1);
    $item = $group ? "$group$val" : $val;
    $item =~ s/""/\//;
    if($line =~ /name_context=(".*?")/)
    {
      print infoblock() . "trc($1, $val);\n";
    }
    else
    {
      print infoblock() . "tr($val);\n";
    }
  }
  elsif($line =~ /<chunk\s+id=(".*?")/)
  {
    $chunk = fix($1);
  }
  elsif($line =~ /<group.*\s+name=(".*?")/)
  {
    my $gr = fix($1);
    $group = $group ? "$group$gr" : $gr;
    $group =~ s/\"\"/\//;
    if($line =~ /name_context=(".*?")/)
    {
      print infoblock() . "trc($1,$gr);\n";
    }
    else
    {
      print infoblock() . "tr($gr);\n";
    }
  }
  elsif($line =~ /<label.*\s+text=" "/)
  {
    print infoblock("empty label") . "\n";
  }
  elsif($line =~ /<label.*\s+text=(".*?")/)
  {
    my $text = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print infoblock("label $text") ."trc($1,$text);\n";
    }
    else
    {
      print infoblock("label $text") . "tr($text);\n";
    }
  }
  elsif($line =~ /<text.*\s+text=(".*?")/)
  {
    my $n = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print infoblock("text $n") . "trc($1,$n);\n";
    }
    else
    {
      print infoblock("text $n") . "tr($n);\n";
    }
  }
  elsif($line =~ /<check.*\s+text=(".*?")/)
  {
    my $n = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print infoblock("check $n") . "trc($1,$n);\n";
    }
    else
    {
      print infoblock("check $n") . "tr($n);\n";
    }
  }
  elsif($line =~ /<role.*\s+text=(".*?")/)
  {
    my $n = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print infoblock("role $n") . "trc($1,$n);\n";
    }
    else
    {
      print infoblock("role $n") . "tr($n);\n";
    }
  }
  elsif($line =~ /<optional.*\s+text=(".*?")/)
  {
    my $n = fix($1);
    if($line =~ /text_context=(".*?")/)
    {
      print infoblock("optional $n") . "trc($1,$n);\n";
    }
    else
    {
      print infoblock("optional $n") . "tr($n);\n";
    }
  }
  elsif($line =~ /<(combo|multiselect).*\s+text=(".*?")/)
  {
    my ($type,$n) = ($1,fix($2));
    $combo_n = $n;
    $combo_type = $type;
    $vctx = ($line =~ /values_context=(".*?")/) ? $1 : undef;
    # text
    my $tctx = ($line =~ /text_context=(".*?")/) ? $1 : undef;
    print infoblock("$type $n") . ($tctx ? " trc($tctx, $n);" : " tr($n);");
    # display_values / values
    my $sp = ($line =~ /delimiter="(.*?)"/) ? $1 : ($type eq "combo" ? ",":";");
    my $vals = ($line =~ / display_values="(.*?)"/) ? $1 : ($line =~ /values="(.*?)"/) ? $1 : undef;
    $vals = undef if ($line =~ /values_no_i18n="true"/);
    if($vals)
    {
      my @combo_values = split "\Q$sp\E" ,$vals;
      foreach my $val (@combo_values)
      {
        next if $val =~ /^[0-9-]+$/; # search for non-numbers
        $val = fix($val);
        print infoblock("$type $n display value") . ($vctx ? " trc($vctx, \"$val\");" : " tr(\"$val\");");
      }
    }
    print "\n";
  }
  elsif(!$comment && $line =~ /<list_entry/)
  {
    my $vctxi = ($line =~ /value_context=(".*?")/) ? $1 : $vctx;
    my $value = ($line =~ /value=(".*?")/) ? $1 : undef;
    if($line =~ /[^.]display_value=(".*?")/)
    {
      my $val = fix($1);
      print infoblock("entry $value display value") . ($vctxi ? " trc($vctxi, $val);" : " tr($val);");
    }
    else
    {
      my $val = fix($value);
      print infoblock("entry $value display value") . ($vctxi ? " trc($vctxi, $val);" : " tr($val);");
    }
    if($line =~ /short_description=(".*?")/)
    {
      my $val = fix($1);
      print infoblock("entry $value short description") . "tr($val);";
    }
    print "\n";
  }
  elsif($line =~ /<\/group>/)
  {
    $group = 0 if !($group =~ s/(.*\/).*?$//);
    print "\n";
  }
  elsif($line =~ /<\/item>/)
  {
    $item = "";
    print "\n";
  }
  elsif($line =~ /<\/chunk>/)
  {
    $chunk = "";
  }
  elsif($line =~ /<\/(combo|multiselect)/)
  {
    $combo_n = "";
    print "\n";
  }
  elsif(!$line)
  {
    print "\n";
  }
  elsif($line =~ /^\s*$/
     || $line =~ /<separator *\/>/
     || $line =~ /<space *\/>/
     || $line =~ /<\/?optional>/
     || $line =~ /<key/
     || $line =~ /<presets/
     || $line =~ /<checkgroup/
     || $line =~ /<\/checkgroup/
     || $line =~ /<\/presets/
     || $line =~ /roles/
     || $line =~ /<link wiki=/
     || $line =~ /href=/
     || $line =~ /<!--/
     || $line =~ /<\?xml/
     || $line =~ /-->/
     || $line =~ /<\/?chunk/
     || $line =~ /<reference/
     || $line =~ /<preset_link/
     || $line =~ /<item_separator\/>/
     || $comment)
  {
    $line =~ s/[ \t]+((?:short)?description) *= *"([^"]+)/*\/ \/* $1 *\/ tr("$2"); \/*/g;
    print "/* $line */\n";
  }
  else
  {
    print "/* unparsed line $line */\n";
    print STDERR "/* unparsed line $linenr $line */\n";
    $result = 20
  }

  # note, these two must be in this order ore oneliners aren't handled
  $comment = 1 if($line =~ /<!--/);
  $comment = 0 if($line =~ /-->/);
}

print "}}\n";
exit($result) if $result;
