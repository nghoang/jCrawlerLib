package com.ngochoang.CrawlerLib;

import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.ngochoang.ParseObject.GeneralObject;

public class ParserLib {

    private String version = "17";
    private Vector<GeneralObject> tempValue;
    private static String tempContent;
    private boolean isSearchDeep = true;
    private boolean getBoundTag = true;

    public String GetVersion() {
        return version;
    }

    public Vector<GeneralObject> GetTagValue(String content, String tag,
            String id, String name, String cls, String attr) {
        tempContent = content;
        Parser parser = Parser.createParser(content, "UTF-8");
        tempValue = new Vector<GeneralObject>();
        try {
            for (NodeIterator i = parser.elements(); i.hasMoreNodes();) {
                GetTagValue(i.nextNode(), tag, name, id, cls, attr, "", "");
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }

        return tempValue;
    }

    public GeneralObject GetTagValueSingle(String content, String tag,
            String id, String name, String cls, String attr) {
        Vector<GeneralObject> tags = GetTagValue(content, tag,
                id, name, cls, attr);
        if (tags.isEmpty()) {
            return null;
        } else {
            return tags.elementAt(0);
        }
    }

    private void GetTagValue(Node node, String tg, String name, String id,
            String cls, String attr, String otherAttr, String otherValue)
            throws ParserException {
        if (node instanceof TagNode) {
            TagNode tag = (TagNode) node;
            if (tag.isEndTag() == true) {
                return;
            }
            boolean isCorrect = false;
            if (tag.getTagName().toUpperCase().equals(tg.toUpperCase())) {
                isCorrect = true;
                if (!name.equals("")) {
                    isCorrect = false;
                    if (tag.getAttribute("name") != null) {
                        if (name.startsWith("*") && name.endsWith("*")) {
                            name = name.replace("*", "");
                            if (tag.getAttribute("name").indexOf(name) >= 0) {
                                isCorrect = true;
                            }
                        } else if (name.startsWith("*")) {
                            name = name.replace("*", "");
                            if (tag.getAttribute("name").endsWith(name)) {
                                isCorrect = true;
                            }
                        } else if (name.endsWith("*")) {
                            name = name.replace("*", "");
                            if (tag.getAttribute("name").startsWith(name)) {
                                isCorrect = true;
                            }
                        } else if (tag.getAttribute("name").equals(name)) {
                            isCorrect = true;
                        }
                    }
                }

                if (!id.equals("")) {
                    isCorrect = false;
                    if (tag.getAttribute("id") != null) {
                        if (id.startsWith("*") && id.endsWith("*")) {
                            id = id.replace("*", "");
                            if (tag.getAttribute("id").indexOf(id) >= 0) {
                                isCorrect = true;
                            }
                        } else if (id.startsWith("*")) {
                            id = id.replace("*", "");
                            if (tag.getAttribute("id").endsWith(id)) {
                                isCorrect = true;
                            }
                        } else if (id.endsWith("*")) {
                            id = id.replace("*", "");
                            if (tag.getAttribute("id").startsWith(id)) {
                                isCorrect = true;
                            }
                        } else if (tag.getAttribute("id").equals(id)) {
                            isCorrect = true;
                        }
                    }
                }

                if (!cls.equals("")) {
                    isCorrect = false;
                    if (tag.getAttribute("class") != null) {
                        if (cls.startsWith("*") && cls.endsWith("*")) {
                            cls = cls.replace("*", "");
                            if (tag.getAttribute("class").indexOf(cls) >= 0) {
                                isCorrect = true;
                            }
                        } else if (cls.startsWith("*")) {
                            cls = cls.replace("*", "");
                            if (tag.getAttribute("class").endsWith(cls)) {
                                isCorrect = true;
                            }
                        } else if (cls.endsWith("*")) {
                            cls = cls.replace("*", "");
                            if (tag.getAttribute("class").startsWith(cls)) {
                                isCorrect = true;
                            }
                        } else if (tag.getAttribute("class").equals(cls)) {
                            isCorrect = true;
                        }
                    }
                }

                if (!otherAttr.equals("")) {
                    isCorrect = false;
                    if (tag.getAttribute(otherAttr) != null) {
                        if (otherAttr.startsWith("*") && otherAttr.endsWith("*")) {
                            otherAttr = otherAttr.replace("*", "");
                            if (tag.getAttribute(otherAttr).indexOf(otherAttr) >= 0) {
                                isCorrect = true;
                            }
                        } else if (otherAttr.startsWith("*")) {
                            otherAttr = otherAttr.replace("*", "");
                            if (tag.getAttribute(otherAttr).endsWith(otherAttr)) {
                                isCorrect = true;
                            }
                        } else if (otherAttr.endsWith("*")) {
                            otherAttr = otherAttr.replace("*", "");
                            if (tag.getAttribute(otherAttr).startsWith(otherAttr)) {
                                isCorrect = true;
                            }
                        } else if (tag.getAttribute(otherAttr).equals(otherValue)) {
                            isCorrect = true;
                        }
                    }
                }
            }

            if (isCorrect == true) {
                GeneralObject go = new GeneralObject();
                go.setTagName(tg);
                if (tag.getAttribute("name") != null) {
                    go.setName(tag.getAttribute("name"));
                } else {
                    go.setName("");
                }

                if (tag.getAttribute("id") != null) {
                    go.setId(tag.getAttribute("id"));
                } else {
                    go.setId("");
                }

                if (tag.getAttribute("class") != null) {
                    go.setCls(tag.getAttribute("class"));
                } else {
                    go.setCls("");
                }

                if (tag.getEndTag() != null) {
                    int BlockStartPos = 0;
                    int BlockEndPos = 0;
                    if (getBoundTag == true) {
                        BlockStartPos = tag.getTagBegin();
                        BlockEndPos = tag.getEndTag().getEndPosition();
                    } else {
                        BlockStartPos = tag.getTagEnd();
                        BlockEndPos = tag.getEndTag().getStartPosition();
                    }
                    go.setBeginPos(BlockStartPos);
                    go.setEndPos(BlockEndPos);

                    if (BlockStartPos >= BlockEndPos) {
                        go.setInnerText("");
                    } else {
                        go.setInnerText(tempContent.substring(BlockStartPos,
                                BlockEndPos));
                    }
                } else {
                    go.setInnerText("");
                }
                go.setInnerPlainText(Utilities.GetPlainText(go.getInnerText()));
                if (!attr.equals("")) {
                    go.setValue(tag.getAttribute(attr));
                    if (tag.getAttribute("name") != null) {
                        go.setName(tag.getAttribute("name"));
                    }
                    tempValue.add(go);
                } else {
                    tempValue.add(go);
                }
            }

            if (isCorrect == false || isSearchDeep == true) {
                NodeList nl = tag.getChildren();
                if (null != nl) {
                    for (NodeIterator i = nl.elements(); i.hasMoreNodes();) {
                        GetTagValue(i.nextNode(), tg, name, id, cls, attr,
                                otherAttr, otherValue);
                    }
                }
            }
        }
    }

    public Vector<GeneralObject> GetBlock(String content, String tag,
            String id, String name, String cls, String otherAttr,
            String otherValue) {
        tempContent = content;
        Parser parser = Parser.createParser(content, "UTF-8");
        tempValue = new Vector<GeneralObject>();
        try {
            for (NodeIterator i = parser.elements(); i.hasMoreNodes();) {
                GetTagValue(i.nextNode(), tag, name, id, cls, "", otherAttr,
                        otherValue);
            }
        } catch (ParserException e) {
            Utilities.WriteLogTrace(e);
            e.printStackTrace();
        }
        return tempValue;
    }

    public GeneralObject GetBlockSingle(String content, String tag,
            String id, String name, String cls, String otherAttr,
            String otherValue) {
        Vector<GeneralObject> blocks = GetBlock(content, tag,
                id, name, cls, otherAttr,
                otherValue);
        if (blocks.isEmpty()) {
            return null;
        } else {
            return blocks.elementAt(0);
        }
    }

    public void setSearchDeep(boolean isSearchDeep) {
        this.isSearchDeep = isSearchDeep;
    }

    public void setGetBoundTag(boolean getBoundTag) {
        this.getBoundTag = getBoundTag;
    }
}
