use std::cell::RefCell;
use std::fmt::Display;
use std::rc::{Rc,Weak};

struct Node<T> {
    data: T,
    parents: Vec<Weak<RefCell<Node<T>>>>,
}
impl<T> Node<T> {
    pub fn new(data: T) -> Self {
        Self { data: data, parents: vec![]}
    }
    pub fn add_parent(&mut self, parent: Weak<RefCell<Node<T>>>) {
        self.parents.push(parent);
    }

    pub fn set(&mut self, new_data: T) {
        self.data = new_data;
    }
}

// Used for pretty-printing a `Node`
impl<T: Display> Display for Node<T> {
    fn fmt(&self, w: &mut std::fmt::Formatter) -> std::result::Result<(), std::fmt::Error> {
        write!(w, "Node[data: {}, parents: [ ", self.data)?;
        for p in &self.parents {
            // need an intermediate variable otherwise we cannot borrow because it is collected
            let p_ref = p.upgrade().expect("my parent is dead :(");
            write!(w, "{}, ", p_ref.borrow())?;
        }
        write!(w, "]]")
    }
}

fn main() {
    // Rc -> Reference Counter -> allow me to have multiple things pointing at my node
    // RefCell -> allow me to mutate the cell even if some other stuff is pointing at it
    let mut graph:Vec<Rc<RefCell<Node<usize>>>> = vec![];

    // I create 5 nodes, each having the (at most) 2 previous as parents
    for i in 0..5 {
        let mut n = Node::new(i * 10);

        if i > 0 {
            n.add_parent(Rc::downgrade(&graph[i-1]));
        }

        if i > 1 {
            n.add_parent(Rc::downgrade(&graph[i-2]));
        }

        graph.push(Rc::new(RefCell::new(n)));
    }

    // let's see the nodes
    for node in &graph {
        println!("{}", &node.borrow())
    }

    // Now change the content of third node
    //
    // Note: since I have a `Rc<RefCell<Node>>`, I do `x.borrow_mut()` to get a `&mut Node`
    graph[2].borrow_mut().set(400);

    // Let see the value of the first parent of the first parent of the fifth node (aka the node I modified)
    //
    // This is "a little" verbose:
    // - `borrow()` gives me a `Ref<Node>` from a `Rc<RefCell<Node>` (so I can call `parents`)
    // - `upgrade().expect` gives me a `Rc<RefCell<Node>>` from a `Weak<RefCell<Node>>`
    // - I need intermediate vars to avoid `parent` being "collected" before it is borrowed
    //   (same with `pparent` for `println`)
    let parent = graph[4].borrow().parents[0].upgrade().expect("parent dead :(");
    let pparent = parent.borrow().parents[0].upgrade().expect("pparent dead :(");
    println!("----");
    println!("{}", pparent.borrow());
    println!("----");
}
