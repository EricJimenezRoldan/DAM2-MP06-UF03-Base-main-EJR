declare option output:method "xml";
declare option output:indent "yes";

<posts>{
  for $p in collection()/posts/row[@PostTypeId='1']
  order by number($p/@ViewCount) descending
  return <post>
    <title>{$p/@Title}</title>
    <views>{$p/@ViewCount}</views>
  </post>
}</posts>
