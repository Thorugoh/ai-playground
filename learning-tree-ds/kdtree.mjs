// ============================================================================
//  k-d Tree — a teaching implementation in plain JavaScript (ES modules).
//
//  Run it:   node kdtree.mjs
//
//  A k-d tree organizes points in k-dimensional space so that nearest-neighbor
//  and range queries can prune away large regions instead of scanning every
//  point. The key trick: each level of the tree splits on a different axis,
//  cycling axis = depth % k.
//
//  This file is written for reading top-to-bottom. Sections:
//    1. Point helpers (distance math)
//    2. The KDTree class: build, kNN search, range search, insert
//    3. A brute-force oracle (to PROVE the tree returns correct answers)
//    4. A demo / self-test you can run
// ============================================================================

// ----------------------------------------------------------------------------
// 1. Point helpers
//    A "point" here is just a plain array of numbers, e.g. [x, y] or [x, y, z].
// ----------------------------------------------------------------------------

/** Squared Euclidean distance. We compare squared distances to avoid sqrt:
 *  if d1² < d2² then d1 < d2, so ordering is preserved and it's faster. */
function distanceSquared(a, b) {
  let sum = 0;
  for (let i = 0; i < a.length; i++) {
    const diff = a[i] - b[i];
    sum += diff * diff;
  }
  return sum;
}

// ----------------------------------------------------------------------------
// 2. The KDTree
// ----------------------------------------------------------------------------

/** A single tree node. `point` is this node's k-d point; `axis` is the
 *  dimension this node splits on; `left`/`right` are child subtrees. */
class KDNode {
  constructor(point, axis) {
    this.point = point;
    this.axis = axis;
    this.left = null;
    this.right = null;
  }
}

export class KDTree {
  /**
   * @param {number[][]} points  array of points, all with the same length k.
   */
  constructor(points = []) {
    if (points.length === 0) {
      this.k = 0;
      this.root = null;
      return;
    }
    this.k = points[0].length; // dimensionality, inferred from the first point
    // Build a balanced tree from all points at once (the "good" way).
    this.root = this.#build(points.slice(), 0);
  }

  // ----- BUILD (balanced, median-based) -------------------------------------
  // At each level we choose the axis (= depth % k), find the MEDIAN point along
  // that axis, make it the node, and recurse on the two halves. Splitting on the
  // median keeps the tree balanced => height O(log n).
  #build(points, depth) {
    if (points.length === 0) return null;

    const axis = depth % this.k;

    // Sort by the current axis so the middle element is the median.
    // (Sorting each level => O(n log^2 n) build. Simple and fine for learning.
    //  A linear-time median selection would make it O(n log n).)
    points.sort((a, b) => a[axis] - b[axis]);

    const mid = Math.floor(points.length / 2);
    const node = new KDNode(points[mid], axis);

    node.left = this.#build(points.slice(0, mid), depth + 1);
    node.right = this.#build(points.slice(mid + 1), depth + 1);
    return node;
  }

  // ----- INSERT (one point at a time, BST-style) ----------------------------
  // Useful to understand the structure, but inserting in a bad order can
  // unbalance the tree. Prefer rebuilding from all points when you can.
  insert(point) {
    if (this.root === null) {
      this.k = point.length;
      this.root = new KDNode(point, 0);
      return;
    }
    let node = this.root;
    let depth = 0;
    while (true) {
      const axis = depth % this.k;
      if (point[axis] < node.point[axis]) {
        if (node.left === null) {
          node.left = new KDNode(point, (depth + 1) % this.k);
          return;
        }
        node = node.left;
      } else {
        if (node.right === null) {
          node.right = new KDNode(point, (depth + 1) % this.k);
          return;
        }
        node = node.right;
      }
      depth++;
    }
  }

  // ----- NEAREST NEIGHBOR ---------------------------------------------------
  /** Returns the single closest point to `query`, or null if the tree is empty. */
  nearest(query) {
    const result = this.kNearest(query, 1);
    return result.length ? result[0].point : null;
  }

  // ----- k-NEAREST NEIGHBORS ------------------------------------------------
  /**
   * Returns the `k` closest points to `query`, sorted nearest-first.
   * @returns {{point: number[], distance: number}[]}
   *
   * We keep a small "best so far" list of size k. The pruning rule:
   *   - Always recurse into the side of the splitting plane that `query` is on.
   *   - Only recurse into the OTHER side if the perpendicular distance from
   *     `query` to the splitting plane is closer than our current worst best.
   *     If it's farther, the entire other subtree cannot contain a closer
   *     point, so we skip it. THIS is what makes k-d trees fast.
   */
  kNearest(query, k = 1) {
    // `best` holds up to k entries; we keep it as a simple sorted array so the
    // last element is always the current worst (farthest) of our keepers.
    const best = [];

    const consider = (point) => {
      const d = distanceSquared(query, point);
      if (best.length < k) {
        best.push({ point, distance: d });
        best.sort((a, b) => a.distance - b.distance);
      } else if (d < best[best.length - 1].distance) {
        best[best.length - 1] = { point, distance: d };
        best.sort((a, b) => a.distance - b.distance);
      }
    };

    const worstDist = () =>
      best.length < k ? Infinity : best[best.length - 1].distance;

    const search = (node) => {
      if (node === null) return;

      consider(node.point);

      const axis = node.axis;
      const diff = query[axis] - node.point[axis]; // signed distance to the plane

      // Decide which child is "near" (same side as the query) vs "far".
      const near = diff < 0 ? node.left : node.right;
      const far = diff < 0 ? node.right : node.left;

      // Always explore the near side first — it most likely holds good points,
      // which tightens `worstDist()` and lets us prune the far side harder.
      search(near);

      // PRUNING TEST: could the far side hold anything within our current radius?
      // Compare squared perpendicular distance to the plane vs our worst keeper.
      if (diff * diff < worstDist()) {
        search(far);
      }
    };

    search(this.root);
    // Convert stored squared distances back to real distances for the caller.
    return best.map((b) => ({ point: b.point, distance: Math.sqrt(b.distance) }));
  }

  // ----- RANGE SEARCH (radius) ----------------------------------------------
  /**
   * Returns every point within `radius` of `center`.
   * Same pruning idea: skip a subtree if its entire half-space is farther
   * than `radius` from the center along the splitting axis.
   */
  withinRadius(center, radius) {
    const found = [];
    const r2 = radius * radius;

    const search = (node) => {
      if (node === null) return;

      if (distanceSquared(center, node.point) <= r2) {
        found.push(node.point);
      }

      const axis = node.axis;
      const diff = center[axis] - node.point[axis];

      // If the plane is within `radius` along this axis, BOTH sides may have
      // matches. Otherwise only the side the center is on can.
      if (diff < 0) {
        search(node.left);
        if (diff * diff <= r2) search(node.right);
      } else {
        search(node.right);
        if (diff * diff <= r2) search(node.left);
      }
    };

    search(this.root);
    return found;
  }
}

// ----------------------------------------------------------------------------
// 3. Brute-force oracle — the "ground truth" we check the tree against.
// ----------------------------------------------------------------------------

function bruteKNearest(points, query, k) {
  return points
    .map((p) => ({ point: p, distance: Math.sqrt(distanceSquared(query, p)) }))
    .sort((a, b) => a.distance - b.distance)
    .slice(0, k);
}

// ----------------------------------------------------------------------------
// 4. Demo / self-test. Runs only when you execute this file directly.
// ----------------------------------------------------------------------------

function randomPoints(n, k, max = 100) {
  const pts = [];
  for (let i = 0; i < n; i++) {
    pts.push(Array.from({ length: k }, () => Math.round(Math.random() * max)));
  }
  return pts;
}

function demo() {
  console.log("=== k-d Tree demo ===\n");

  // A small, hand-checkable 2-D example (the same points used in the lesson).
  const points = [[5, 4], [2, 6], [7, 2], [8, 5], [3, 8], [1, 3]];
  const tree = new KDTree(points);

  const query = [6, 3];
  console.log("Points:", JSON.stringify(points));
  console.log("Query: ", JSON.stringify(query));
  console.log("Nearest:", JSON.stringify(tree.nearest(query)), "(expect [7,2] or [5,4])\n");

  console.log("3 nearest to", JSON.stringify(query) + ":");
  for (const { point, distance } of tree.kNearest(query, 3)) {
    console.log("   ", JSON.stringify(point), " dist =", distance.toFixed(3));
  }

  console.log("\nPoints within radius 3 of", JSON.stringify(query) + ":");
  console.log("   ", JSON.stringify(tree.withinRadius(query, 3)));

  // ---- Correctness check: 1000 random queries vs brute force ----
  console.log("\n=== Randomized correctness test (kNN == brute force) ===");
  const big = randomPoints(2000, 3); // 2000 points in 3-D
  const bigTree = new KDTree(big);
  let failures = 0;
  const TRIALS = 1000;
  const K = 5;
  for (let t = 0; t < TRIALS; t++) {
    const q = randomPoints(1, 3)[0];
    const treeAns = bigTree.kNearest(q, K).map((r) => r.distance.toFixed(6));
    const bruteAns = bruteKNearest(big, q, K).map((r) => r.distance.toFixed(6));
    // Compare the SORTED DISTANCES (ties make exact point order ambiguous).
    if (JSON.stringify(treeAns) !== JSON.stringify(bruteAns)) failures++;
  }
  console.log(`${TRIALS} random ${K}-NN queries on 2000 points in 3-D`);
  console.log(failures === 0 ? "✅ ALL MATCH brute force" : `❌ ${failures} mismatches`);
}

// `import.meta.url` trick: run demo only when executed, not when imported.
if (import.meta.url === `file://${process.argv[1]}`) {
  demo();
}
