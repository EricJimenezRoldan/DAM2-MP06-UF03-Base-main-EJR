declare option output:method "xml";
declare option output:indent "yes";

<users>{
  for $post in collection()/posts/row[@PostTypeId='1']
  group by $ownerId := $post/@OwnerUserId
  let $user := collection()/users/row[@Id = $ownerId]
  order by count($post) descending
  return
    <user>
      <name>{$user/@DisplayName/string()}</name>
      <questions>{count($post)}</questions>
    </user>
}</users>
