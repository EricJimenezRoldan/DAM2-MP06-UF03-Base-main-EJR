declare option output:method "xml";
declare option output:indent "yes";

<posts>{
  for $q in collection()/posts/row[@PostTypeId='1']
  order by number($q/@Score) descending
  let $answers := collection()/posts/row[@ParentId=$q/@Id and @PostTypeId='2']
  let $highestAnswer := (
      for $a in $answers
      order by number($a/@Score) descending
      return $a
  )[1]
  return
    <post>
      <title>{$q/@Title}</title>
      <score>{$q/@Score}</score>
      <topAnswer>
        <score>{$highestAnswer/@Score}</score>
        <body>{$highestAnswer/@Body}</body>
      </topAnswer>
    </post>
}</posts>
