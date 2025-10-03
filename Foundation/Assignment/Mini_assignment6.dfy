/*--------------------------------------*/
/* COMP 1600/6260 Tutorial 7 Exercise 1 */
/*--------------------------------------*/

datatype Tree = Emp | Node (l: Tree, i: int, r: Tree)

function  is_in (i: int, t: Tree): bool  {
    match t {
        case Emp => false
        case Node (l, j, r) => i == j || is_in(i, l) || is_in (i, r)
    }
}

ghost predicate is_bst (t: Tree) {
    match t {
        case Emp => true
        case Node (l, i, r) =>
          (forall j: int :: is_in (j, l) ==> j < i) &&
          (forall j: int :: is_in (j, r) ==> j > i) &&
          is_bst(l) && is_bst(r)
    }
}

function  lookup (j: int, t: Tree):  bool  {
    match t {
        case Emp => false
        case Node (l, i, r) =>
        (i == j) || (j < i && lookup(j, l)) || (j > i && lookup(j, r))
    }
}


/* The lemma is formulated as follows: */
lemma is_in_lookup (j: int, t: Tree)
requires is_bst(t)
ensures is_in(j, t) == lookup(j, t)
{
  match t
  case Emp =>
    // is_in(j,Emp) == false and lookup(j,Emp) == false
  case Node(l,i,r) =>
    if i == j {
      // both is_in and lookup are true
    } else if j < i {
      // show right subtree cannot contain j in a BST when j<i
      assert (forall k:int :: is_in(k, r) ==> k > i);
      assert !is_in(j, r) by {
        // if is_in(j,r) then j>i by BST, contradicts j<i
      }
      // equivalence reduces to left
      reveal_is_in_lookup_left(j, l, r, i);
      // IH on left
      assert is_bst(l);
      assert is_in(j, t) == is_in(j, l);
      assert lookup(j, t) == lookup(j, l);
      calc {
        is_in(j, t);
        == { }
        is_in(j, l);
        == { is_in_lookup(j, l); }
        lookup(j, l);
        == { }
        lookup(j, t);
      }
    } else {
      // j > i: symmetric, left cannot contain j
      assert (forall k:int :: is_in(k, l) ==> k < i);
      assert !is_in(j, l) by { }
      // IH on right
      assert is_bst(r);
      assert is_in(j, t) == is_in(j, r);
      assert lookup(j, t) == lookup(j, r);
      calc {
        is_in(j, t);
        == { }
        is_in(j, r);
        == { is_in_lookup(j, r); }
        lookup(j, r);
        == { }
        lookup(j, t);
      }
    }
}

// Tiny helper to make the reasoning steps clear to Dafny in the j<i branch.
lemma reveal_is_in_lookup_left(j:int, l:Tree, r:Tree, i:int)
  ensures (is_in(j, Node(l,i,r)) == (j==i || is_in(j,l) || is_in(j,r)))
  ensures (lookup(j, Node(l,i,r)) == (j==i || (j<i && lookup(j,l)) || (j>i && lookup(j,r))))
{}

/* Proof by hand:
We prove: for any j and t, if is_bst(t) then is_in(j,t) == lookup(j,t).
Induction on t.

Base t=Emp: is_in = false, lookup = false, equal.

Step t=Node(l,i,r): consider cases on j and i.
1) j==i: both sides true by definition.
2) j<i: In a BST, all elements of r are > i, hence cannot be j (<i). Thus is_in(j,t) reduces to is_in(j,l).
   Similarly lookup(j,t) reduces to lookup(j,l). Apply IH on l.
3) j>i: Symmetric to (2), reduce to right subtree and apply IH on r.
QED.
*/


/*--------------------------------------*/
/* COMP 1600/6260 Tutorial 7 Exercise 2 */
/*--------------------------------------*/

function insert (j: int, t: Tree): Tree {
    match t {
        case Emp => Node (Emp, j, Emp)
        case Node (l, i, r) =>
          if (j < i) then Node (insert (j, l), i, r)
          else if (j > i) then Node (l, i, insert (j, r))
          else t
    }
}

/* the first lemma */
lemma insert_is_in (j: int, t: Tree)
ensures is_in(j, insert(j, t))
{
  match t
  case Emp =>
    // insert(j,Emp)=Node(Emp,j,Emp); is_in(j,Node(...)) holds
  case Node(l,i,r) =>
    if j < i {
      assert is_in(j, insert(j, l)); // IH
      // Then j is in the rebuilt Node(insert(l), i, r)
    } else if j > i {
      assert is_in(j, insert(j, r)); // IH
      // Then j is in the rebuilt Node(l, i, insert(r))
    } else {
      // j==i, insert returns t and j is at root
    }
}

/*  proof by hand:
Induction on t.
Base t=Emp: insert gives a single-node, which contains j.
Step t=Node(l,i,r):
- If j<i: insert recurses into l; by IH j∈insert(j,l); thus j∈Node(insert(l),i,r).
- If j>i: symmetric on r.
- If j=i: insert returns t unchanged and j is at the root. QED.
*/


/* the second lemma */
lemma insert_only_in (j: int, t: Tree)
ensures forall x:int :: is_in(x, insert(j, t)) ==> (x == j) || is_in(x, t)
{
  match t
  case Emp =>
    // insert => single-node; the only new element is j
  case Node(l,i,r) =>
    if j < i {
      // Any element of new left is either old-left or j (IH), others unchanged
      assert forall x:int :: is_in(x, insert(j, l)) ==> x==j || is_in(x, l) by { }
    } else if j > i {
      assert forall x:int :: is_in(x, insert(j, r)) ==> x==j || is_in(x, r) by { }
    } else {
      // j==i: tree unchanged; every element was already in t
    }
}


/*--------------------------------------*/
/* COMP 1600/6260 Tutorial 7 Exercise 3 */
/*--------------------------------------*/

/* Helper Lemma 1:
   If every element of l is < i and j < i, then after inserting j into l,
   every element of insert(j,l) is still < i. */
lemma insert_left_all_lt (j:int, l:Tree, i:int)
  requires (forall x:int :: is_in(x, l) ==> x < i)
  requires j < i
  ensures  (forall x:int :: is_in(x, insert(j, l)) ==> x < i)
{
  // Classify elements of insert(j,l): either x==j or x comes from l
  insert_only_in(j, l);
  forall x | is_in(x, insert(j, l)) ensures x < i {
    if x == j {
      // From the branch guard j < i
    } else {
      assert is_in(x, l);
      assert (forall y:int :: is_in(y, l) ==> y < i);
    }
  }
}

/* Helper Lemma 2:
   If every element of r is > i and i < j, then after inserting j into r,
   every element of insert(j,r) is still > i. */
lemma insert_right_all_gt (j:int, r:Tree, i:int)
  requires (forall x:int :: is_in(x, r) ==> x > i)
  requires i < j
  ensures  (forall x:int :: is_in(x, insert(j, r)) ==> x > i)
{
  insert_only_in(j, r);
  forall x | is_in(x, insert(j, r)) ensures x > i {
    if x == j {
      // From the branch guard i < j
    } else {
      assert is_in(x, r);
      assert (forall y:int :: is_in(y, r) ==> y > i);
    }
  }
}

/* Main Lemma: insertion preserves the BST property. */
lemma insert_bst (j: int, t: Tree)
  requires is_bst(t)
  ensures  is_bst(insert(j, t))
  decreases t
{
  match t
  case Emp =>
    // insert(Emp) yields a singleton, which is a BST

  case Node(l, i, r) =>
    if j < i {
      // Shape: insertion happens in the left subtree
      assert insert(j, Node(l, i, r)) == Node(insert(j, l), i, r);

      // Facts from is_bst(t)
      assert is_bst(l);
      assert is_bst(r);
      assert (forall x:int :: is_in(x, l) ==> x < i);
      assert (forall x:int :: is_in(x, r) ==> x > i);

      // IH on the left subtree
      insert_bst(j, l);

      // Cross-inequality for the new left subtree
      insert_left_all_lt(j, l, i);

      // Together these establish is_bst(Node(insert(j,l), i, r))

    } else if j > i {
      // Shape: insertion happens in the right subtree
      assert insert(j, Node(l, i, r)) == Node(l, i, insert(j, r));

      // Facts from is_bst(t)
      assert is_bst(l);
      assert is_bst(r);
      assert (forall x:int :: is_in(x, l) ==> x < i);
      assert (forall x:int :: is_in(x, r) ==> x > i);

      // IH on the right subtree
      insert_bst(j, r);

      // Cross-inequality for the new right subtree
      insert_right_all_gt(j, r, i);

      // Hence is_bst(Node(l, i, insert(j,r)))

    } else {
      // j == i: insertion returns the original tree, which is already a BST
      assert insert(j, Node(l, i, r)) == Node(l, i, r);
    }
}



/*--------------------------------------*/
/* COMP 1600/6260 Tutorial 7 Exercise 4 */
/*--------------------------------------*/

function min (t: Tree) : int
requires t != Emp {
    match t { case Node (Emp, i, r) =>  i
               case Node (l, i, r) =>  min(l)
    }
}

function root_elt (t: Tree): int
requires t != Emp {
  match t {
    case Node (l, i, r) => i
  }
}

/* your lemma min_in */
lemma min_in (t: Tree)
requires t != Emp
ensures is_in(min(t), t)
{
  match t
  case Node(Emp, i, r) =>
    // min(t)=i and i is at root
  case Node(l, i, r) =>
    // min(t)=min(l); by IH min(l) in l; hence in Node(l,i,r)
    assert is_in(min(l), l) by { min_in(l); }
}


/* min(t) <= root(t) for BSTs */
lemma min_left (t: Tree)
requires t != Emp
requires is_bst(t)
ensures min(t) <= root_elt(t)
{
  match t
  case Node(Emp, i, r) =>
    // min = i <= i
  case Node(l, i, r) =>
    // min(t)=min(l), and every element of l < i by BST
    assert forall x:int :: is_in(x, l) ==> x < i;
    assert is_in(min(l), l) by { min_in(l); }
    // hence min(l) < i, so min(t) <= i
    assert min(l) < i;
}


/* min(t) is the least element of a BST */
lemma min_least (t: Tree)
requires is_bst(t)
ensures forall j: int :: is_in(j, t) ==> min(t) <= j
{
  match t
  case Emp =>
    // vacuous
  case Node(l, i, r) =>
    // Use case analysis on where j lies
    // 1) j in l
    assert forall j:int :: is_in(j, l) ==> min(l) <= j by { min_least(l); }
    // 2) j == i
    assert min(t) <= i by { min_left(Node(l,i,r)); }
    // 3) j in r
    assert forall x:int :: is_in(x, r) ==> x > i;
    assert min(t) <= i by { min_left(Node(l,i,r)); }
    // From i < j for j in r, we get min(t) <= j
    // Finally, combine all three positions:
    assert forall j:int ::
      is_in(j, Node(l,i,r)) ==> (is_in(j,l) || j==i || is_in(j,r));
    // Prove the postcondition by an arbitrary j with a case split:
    forall j | is_in(j, Node(l,i,r))
      ensures min(Node(l,i,r)) <= j
    {
      if is_in(j, l) {
        // min(Node)=min(l) <= j
        assert min(Node(l,i,r)) == min(l);
        assert min(l) <= j;
      } else if j == i {
        assert min(Node(l,i,r)) <= i;
      } else {
        // j in r
        assert is_in(j, r);
        assert i < j;
        // Since min(Node) <= i and i < j, min(Node) <= j
        assert min(Node(l,i,r)) <= i;
      }
    }
}

/*--------------------------------------*/
/* COMP 1600/6260 Tutorial 7 Exercise 5 */
/*--------------------------------------*/

/* We might need some of the lemmas from previous
   exercises. If you want to do this exercise,
   but haven't done one of the earlier exercises
   (that this one depends on), you can assume the
   lemmas from earlier exercises without proof by
   not providing a body, like so: */

lemma some_fact (i: int)
  ensures i + 1 == 1 + i
  /* We omit the opening/closing brace pair '{ .. }'
     deliberately to assume some_fact without
     a proof. */

/* Dafny might tell you that this is dangerous as
   you might do this accidentally and might have
   forgotten the braces. In order to tell Dafny that you
   really want to assume a fact, use the '{:axiom}'
   pragma like so: */

lemma {:axiom} some_other_fact (i: int)
  ensures i + 2 == 2 + i
  /* Again, '{ .. }' omitted on purpose */


function del_min (t: Tree) : Tree
requires t != Emp {
  match t {
    case Node (Emp, i, r) => r
    case Node (l, i, r) => Node (del_min(l), i, r)
  }
}
function delete (j: int, t: Tree): Tree
{
    match t
        case Emp => Emp
        case Node(l , i, r) =>
            if (j < i) then Node (delete(j, l), i, r)
            else if (i < j) then Node (l, i, delete(j, r))
            else if (l == Emp) && (r == Emp) then Emp
            else if (l == Emp) then r
            else if (r == Emp) then l
            else Node (l, min(r), del_min(r))
}

lemma del_min_in(t: Tree)
requires t != Emp
ensures forall j: int :: is_in(j, del_min(t)) ==> is_in (j, t)
{  }

lemma bst_del_min(t: Tree)
requires t != Emp
requires is_bst(t)
// ensures is_bst (del_min(t))
{ /* your proof here */ }

lemma del_min_least (t: Tree)
requires is_bst(t)
requires t != Emp
// ensures forall j: int :: is_in (j, del_min(t)) ==> min(t) < j
{ /* your proof here */ }

lemma delete_in (j: int, k: int, t: Tree)
requires is_bst(t)
// ensures  (is_in(j, delete (k, t)) ==> is_in(j, t))
{ /* your proof here */ }

lemma delete_bst (j: int, t: Tree)
requires is_bst(t)
// ensures is_bst (delete (j, t))
{ /* your proof here */ }
