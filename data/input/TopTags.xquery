declare option output:method "xml";
declare option output:indent "yes";

<tags>{
  for $post in collection()/posts/row[@PostTypeId='1']
  let $tags := tokenize(substring($post/@Tags, 2, string-length($post/@Tags) - 2), '&gt;&lt;')
  for $tag in $tags
  group by $tag
  order by count($tag) descending
  return
    <tag>
      <name>{$tag}</name>
      <count>{count($tag)}</count>
    </tag>
}</tags>

