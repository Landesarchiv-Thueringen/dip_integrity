package thelma.integrity.hashforest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import thelma.integrity.error.InvalidInputException;
import thelma.integrity.util.ChecksumUtil;
import thelma.integrity.util.DateUtil;
import thelma.integrity.util.OrderUtil;
import thelma.integrity.util.TextSerializable;

public class HashForest<T extends HashValue> implements TextSerializable {

  public enum Mode {
    FULL("full"),
    ROOTS("roots");

    private String modeString;

    public String toString() {
      return modeString;
    };

    private Mode(String s) {
      this.modeString = s;
    }

    public static Mode fromString(String s) {
      if (s.equals(Mode.FULL.modeString)) {
        return Mode.FULL;
      } else {
        return Mode.ROOTS;  // assume roots mode as safe case
      }
    }
  }

  private static final long serialVersionUID = 4159661696848135993L;

  public final static String INTEGRITYFILENAME = "integritycomponent-integrity.txt";

  private int version = 1;

  /*
   * Holds a note where the associated ordering information is located.
   * "implicit" is a default value.
   */
  private String orderingInformationLocation = "implicit";

  /*
   * Holds the date and time when this hash forest was first serialized.
   */
  private Date firstSerializedDateTime = null;

  private List<T> leafs = new ArrayList<T>();
  private List<T[]> trees = new ArrayList<T[]>();

  // needed for HashForests in root mode
  private int leafsCount = 0;
  private int treesCount = 0;

  /*
   * Flag that denotes a modified full forest, i.e. this must be set true if
   * new leafs are added and is checked when trees are built.
   */
  private boolean isDirty = false;

  private Mode mode = Mode.FULL;  // full is the default mode

  private void checkIsDirty() {
    if ((isDirty || trees.isEmpty()) && mode.equals(Mode.FULL)) {
      trees = createForest(leafs);
      leafsCount = leafs.size();
      treesCount = trees.size();
      if (isDirty) {
        isDirty = false;
      }
    }
  }

  /**
   * Returns true iff this forest is empty.
   * @return true if forest is empty, otherwise false.
   */
  public boolean isEmpty() {
    return leafs.isEmpty();
  }

  private int[] computeLeafsPerTree(int leafsCount) {
    // compute numer of complete trees by counting the 1-bits in the
    // two-complement-representation
    int trees = Integer.bitCount(leafsCount);
    int[] treeSizes = new int[trees];

    int exp = 0;
    int idx = trees - 1;

    while (idx >= 0) {
      if ( ((1<<exp) & leafsCount) != 0) {
        treeSizes[idx] = 1<<exp;
        --idx;
      }
      ++exp;
    }
    return treeSizes;
  }

  @SuppressWarnings("unchecked")
  private T[] createTree(List<T> leafs) {
    // a complete tree with n leafes has exact 2*n - 1 nodes
    int treeSize = 2 * leafs.size() - 1;
    HashValue[] tree = new HashValue[treeSize];
    for (int treeIdx = treeSize - 1, leafIdx = leafs.size() - 1; leafIdx >= 0; --treeIdx, --leafIdx) {
      tree[treeIdx] = leafs.get(leafIdx);
    }
    // in the array-based (0-based index) representation of a binary tree a
    // parent at index n has the children at 2*n+1 (left) and 2*n+2 (right)
    for (int treeIdx = treeSize - leafs.size() - 1; treeIdx >= 0; --treeIdx) {
      tree[treeIdx] = tree[2*treeIdx+1].concatenate(tree[2*treeIdx+2]);
    }
    return (T[]) tree;
  }

  private List<T[]> createForest(List<T> leafs) {
    int[] leafesPerTree = computeLeafsPerTree(leafs.size());
    List<T[]> forest = new ArrayList<T[]>();
    int startIdx = 0;
    for (int leafSize : leafesPerTree) {
      forest.add(createTree(leafs.subList(startIdx, startIdx + leafSize)));
      startIdx += leafSize;
    }
    return forest;
  }

  /**
   * Update this forest with a new hash value.
   * @param hashValue
   */
  public void update(T hashValue) {
    // TODO: suppress this if mode is ROOTS
    leafs.add(hashValue);
    isDirty = true;
  }

  /**
   * Returns a list of the leafs (i.e. hash values of data items).
   * @return List of leafs.
   */
  public List<T> getLeafs() {
    return Collections.unmodifiableList(leafs);
  }

  /**
   * Returns a list of the roots of all trees in the forest.
   * @return List of roots.
   */
  public List<T> getRoots() {
    checkIsDirty();
    List<T> roots = new ArrayList<T>();
    for (T[] tree : trees) {
      roots.add(tree[0]);
    }
    return roots;
  }

  /**
   * Returns a list of the trees in this forest. Each tree is represented as a
   * list, more specifically each list is a level-order representation of a
   * binary tree (i.e. a parent node at index n has child nodes at 2*n+1
   * (left) and 2*n+2 (right)).
   * @return List of trees represented as lists.
   */
  public List<T[]> getTrees() {
    checkIsDirty();
    List<T[]> treesNew = new ArrayList<T[]>();
    for (T[] tree : trees) {
      treesNew.add(tree.clone());
    }
    return treesNew;
  }


  /**
   * Compares this HashForest object with the other one and returns true iff
   * the forests are equal.
   * @param other The HashForest to compare this to.
   * @return true if the forests are equal, otherwise false.
   */
  public boolean validate(HashForest<T> other) {
    checkIsDirty();
    List<T> roots = getRoots();
    List<T> otherRoots = other.getRoots();
    return roots.equals(otherRoots);
  }



  private boolean contains(T[] haystack, T needle) {
    boolean result = false;
    for (T node : haystack) {
      if (needle.equals(node)) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Returns true if this HashForest is equal to or a superset of the other
   * HashForest. Because hash forests can only grow by adding more leafs to
   * the "end" of a forest, being a superset means that this is an extended
   * version of the other forest and the other forest is a valid sub-forest.
   * @param other A hash forest that should be checked if it is a valid
   *         sub-forest.
   * @return true if this HashForest is equal to or a superset of the other
   * HashForest, othrewise false
   */
  public boolean contains(HashForest<T> other) {
    checkIsDirty();
    if (validate(other)) {
      return true;
    }

    List<T[]> trees = getTrees();
    List<T> otherRoots = other.getRoots();

    boolean result = true;
    for (T root : otherRoots) {
      boolean found = false;
      for (T[] tree : trees) {
        if (contains(tree, root)) {
          found = true;
          break;
        }
      }
      result &= found;
      if (!result) {
        break;
      }
    }

    return result;
  }

  /**
   * Set mode to "roots", i.e. just the roots of all trees are stored.
   * <strong>Note that this setting cannot be reversed</strong>. By keeping
   * only the roots information a <strong>forest cannot be extended in the
   * future</strong>, just validate() is supported.
   */
  public void pruneForest() {
    checkIsDirty();  // recreate trees one last time
    this.mode = Mode.ROOTS;
  }

  /**
   * Returns the mode of this hash forest object. Mode.FULL denotes that this forest
   * can be extended, Mode.ROOTS denotes that this forest can only be used
   * to validate the data.
   * @return hashforest mode
   */
  public Mode getMode() {
    return mode;
  }

  /**
   * Set where the ordering information for this hashforest is stored. Usually
   * this would be a file name e.g. the default file name given in
   * {@link OrderUtil}.ORDERFILENAME, a database table name, or anything
   * suitable to store an ordered list of data items. This information will be
   * written to the serialized integrity information so it's available in
   * deserialized HashForests, however, this isn't more than a note where the
   * ordering information is stored. <strong>Note that the application using
   * this HashForest object is responsible for storing the actual ordering
   * information in the correct place in order to allow validation of the
   * data.</strong>
   *
   * @see getOrderUtilInformationLocation()
   *
   * @param orderingInfo
   *            OrderUtil information storage location
   */
  public void setOrderUtilInformationLocation(String orderingInfo) {
    this.orderingInformationLocation = orderingInfo;
  }

  /**
   * Returns the location where ordering information for this HashForest
   * component is stored.
   *
   * @see setOrderUtilInformationLocation(String orderingInfo)
   *
   * @return OrderUtil information storage location
   */
  public String getOrderUtilInformationLocation() {
    return orderingInformationLocation;
  }

  /**
   * Returns the Date when this hash forest was first serialized.
   * @return Date when this hash forest was first serialized.
   */
  public Date getFirstSerializedDateTime() {
    return (Date) this.firstSerializedDateTime.clone();
  }

  private void updateChecksum(ChecksumUtil cp, String field, String value) {
    cp.update(field.getBytes(Charset.forName("UTF-8")));
    cp.update(value.getBytes(Charset.forName("UTF-8")));
  }

  private void writeChecked(Writer w, ChecksumUtil cp, String field, String value) throws IOException {
    updateChecksum(cp, field, value);
    w.write(field);
    w.write(Const.SEPARATOR);
    w.write(value);
    w.write(Const.NEWLINE);
  }

  @Override
  public void writeTo(Writer w) throws IOException {

    /*
     * Compute a new datetime string as serialization timestamp if either
     * there was none computed before or this hash forest object has been
     * extended. If this hash forest is in ROOTS mode or in FULL mode but
     * has not been extended, reuse the stored datetime.
     */
    Date date = null;
    if ((firstSerializedDateTime == null) || (mode.equals(Mode.FULL) && isDirty)) {
      date = new Date();
    } else {
      date = firstSerializedDateTime;
    }
    String dateFormattet = DateUtil.date2String(date);

    /*
    * TODO: This is not necessary if the hash forest is in FULL mode.
    * It's just the lazy option to provide a trees count.
    */
    checkIsDirty();  // compute trees

    ChecksumUtil cp = null;
    try {
      cp = new ChecksumUtil(MessageDigest.getInstance("SHA-512"));
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Could not instatiate checksum provider");
    }

    writeChecked(w, cp, Const.VERSION, Integer.toString(version));
    writeChecked(w, cp, Const.DATE, dateFormattet);
    writeChecked(w, cp, Const.LEAFS, Integer.toString(leafsCount));
    writeChecked(w, cp, Const.TREES, Integer.toString(treesCount));
    writeChecked(w, cp, Const.ORDER, orderingInformationLocation);
    writeChecked(w, cp, Const.MODE, mode.toString());

    if (mode.equals(Mode.ROOTS)) {
      for (T[] tree : trees) {
        writeChecked(w, cp, Const.ROOT, tree[0].getHexString());
      }
    } else {
      for (T hashValue : leafs) {
        writeChecked(w, cp, Const.LEAF, hashValue.getHexString());
      }
    }

    String checksum = cp.get();
    w.write(Const.CHECKSUM);
    w.write(Const.SEPARATOR);
    w.write(checksum);
    w.write(Const.NEWLINE);

    firstSerializedDateTime = date;
  }


  private String readChecked(ChecksumUtil cp, String line, String expectedField) throws InvalidInputException {
    String[] parts = line.split(Const.SEPARATOR);
    if (parts[0].equals(expectedField)) {
      updateChecksum(cp, parts[0], parts[1]);
      return parts[1];
    } else {
      throw new InvalidInputException("Expected " + expectedField + ", got " + parts[0]);
    }
  }


  @SuppressWarnings("unchecked")
  @Override
  public void readFrom(Reader r) throws IOException, InvalidInputException {
    leafs = new ArrayList<T>();
    trees = new ArrayList<T[]>();

    ChecksumUtil cp = null;
    try {
      cp = new ChecksumUtil(MessageDigest.getInstance("SHA-512"));
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Could not instatiate checksum provider!");
    }
    BufferedReader br = new BufferedReader(r);
    String line = br.readLine();
    String value = readChecked(cp, line, Const.VERSION);
    version = Integer.parseInt(value);

    line = br.readLine();
    value = readChecked(cp, line, Const.DATE);
    try {
      firstSerializedDateTime = DateUtil.string2Date(value);
    } catch (ParseException e1) {
      throw new InvalidInputException("Date format invalid.");
    }

    line = br.readLine();
    value = readChecked(cp, line, Const.LEAFS);
    leafsCount = Integer.parseInt(value);

    line = br.readLine();
    value = readChecked(cp, line, Const.TREES);
    treesCount = Integer.parseInt(value);

    line = br.readLine();
    value = readChecked(cp, line, Const.ORDER);
    orderingInformationLocation = value;

    line = br.readLine();
    value = readChecked(cp, line, Const.MODE);
    mode = Mode.fromString(value);

    try {
      if (mode.equals(Mode.ROOTS)) {
        for (int i = 0; i < treesCount; ++i) {
          line = br.readLine();
          value = readChecked(cp, line, Const.ROOT);
          HashValue hashValue = new SHA512HashValue(value);
          HashValue[] tree = new HashValue[]{hashValue};
          trees.add((T[]) tree);
        }
      } else if (mode.equals(Mode.FULL)) {
        for (int i = 0; i < leafsCount; ++i) {
          line = br.readLine();
          value = readChecked(cp, line, Const.LEAF);
          HashValue hashValue = new SHA512HashValue(value);
          leafs.add((T) hashValue);
        }
      }
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Could not instantiate message digest algorithm for SHA512!");
    }

    String computedChecksum = cp.get();
    line = br.readLine();
    String[] parts = line.split(Const.SEPARATOR);
    if (!parts[0].equals(Const.CHECKSUM) || !parts[1].equals(computedChecksum)) {
      throw new InvalidInputException("Invalid checksum for integrity information!");
    }

    // isDirty = true;
    isDirty = false;
  }

  @Override
  public String toString() {
    StringWriter sw = new StringWriter();
    try {
      writeTo(sw);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sw.toString();
  }

}
