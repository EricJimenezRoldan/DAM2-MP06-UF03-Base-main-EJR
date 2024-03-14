declare option output:method "xml";
declare option output:indent "yes";

<tags>{
  let $posts := collection()/posts/row[@PostTypeId='1']
  for $tags in $posts/@Tags
  let $tag := tokenize(substring($tags, 2, string-length($tags) - 2), '&gt;&lt;')
  group by $tag
  order by count($tag) descending
  return
    <tag>
      <name>{$tag}</name>
      <count>{count($tag)}</count>
    </tag>
}</tags>
