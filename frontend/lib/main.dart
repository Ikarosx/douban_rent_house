import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_html/flutter_html.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';
import 'package:http/http.dart' as http;

class StationInfo {
  String lineNum;
  List<String> stationNames;

  StationInfo(this.lineNum, this.stationNames);
}

class SearchPage extends StatefulWidget {
  @override
  _SearchPageState createState() => _SearchPageState();
}

class _SearchPageState extends State<SearchPage> {
  var posts = [];
  bool agentFilter = true;
  String subwayStation = '全部';
  String lineNum = '全部';
  List<String> lineNums = [];
  List<String> subWayStations = ['全部'];
  Map<String, List<String>> subWayStationsMap = {};
  String community = '';
  String unitType = '全部';

  // 整租
  String rentType = '全部';
  List<String> rentTypes = ['全部', '整租', '合租'];
  int page = 1;
  int size = 10;
  double priceLow = 0.0;
  double priceHigh = 15000.0;
  DateTime date = DateTime.now();

  var _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    getLines();
    searchPosts();
  }

  searchPosts() async {
    // http://192.168.0.110:8999/rent/search
    // 发送请求
    // utf8
    print('请求');
    var params = {
      'page': page,
      'size': size,
      'lineName': lineNum == '全部' ? '' : lineNum,
      'subwayStation': subwayStation == '全部' ? '' : subwayStation,
      'community': community,
      'unitType': unitType == '全部' ? null : unitType,
      'agentFilter': agentFilter,
      'priceLow': priceLow,
      'priceHigh': priceHigh,
      'rentType': rentType
    };
    // 将params转成url参数
    var paramsStr = '';
    params.forEach((key, value) {
      paramsStr += key.toString() + '=' + value.toString() + '&';
    });
    var response = await http.get(
        Uri.parse('http://127.0.0.1:8999/rent/search?' + paramsStr),
        headers: {'Content-Type': 'text/html; charset=utf8'});
    print(utf8.decode(response.bodyBytes));

    // 取出body转成json，其中的records
    setState(() {
      posts = json.decode(utf8.decode(response.bodyBytes))['records'];
      _scrollController.animateTo(0,
          duration: Duration(milliseconds: 200), curve: Curves.ease);
    });
  }

  // 获取线路
  getLines() async {
    setState(() {
      lineNums = [
        '全部',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        '10',
        '11'
      ];
      subWayStationsMap = {
        "1": [
          "全部",
          "世界之窗",
          "会展中心",
          "侨城东",
          "前海湾",
          "华侨城",
          "华强路",
          "后瑞",
          "固戍",
          "国贸",
          "坪洲",
          "大剧院",
          "大新",
          "宝体",
          "宝安中心",
          "岗厦",
          "新安",
          "机场东",
          "桃园",
          "深大",
          "白石洲",
          "科学馆",
          "竹子林",
          "罗湖",
          "老街",
          "西乡",
          "购物公园",
          "车公庙",
          "香蜜湖",
          "高新园",
          "鲤鱼门"
        ],
        "2": [
          "全部",
          "世界之窗",
          "东角头",
          "仙湖路",
          "侨城北",
          "侨香",
          "华强北",
          "后海",
          "大剧院",
          "安托山",
          "岗厦北",
          "市民中心",
          "新秀",
          "景田",
          "水湾",
          "海上世界",
          "海月",
          "深康",
          "湖贝",
          "湾厦",
          "燕南",
          "登良",
          "福田",
          "科苑",
          "红树湾",
          "莲塘",
          "莲塘口岸",
          "莲花西",
          "蛇口港",
          "赤湾",
          "香梅北",
          "香蜜",
          "黄贝岭"
        ],
        "3": [
          "全部",
          "丹竹头",
          "六约",
          "华新",
          "南联",
          "双龙",
          " 吉祥",
          "塘坑",
          "大芬",
          "大运",
          "少年宫",
          "布吉",
          "晒布",
          "木棉湾",
          "横岗",
          "水贝",
          "永湖",
          "爱联",
          "田贝",
          "益田",
          "石厦",
          "福保",
          "福田",
          "红岭",
          "翠竹",
          "老街",
          "草埔",
          "荷坳",
          "莲花村",
          "购物公园",
          "通新岭",
          "龙城广场"
        ],
        "4": [
          "全部",
          "上塘",
          "上梅林",
          "会展中心",
          "少年宫",
          "市民中心",
          "松元厦",
          "民乐",
          "深圳北",
          "清湖",
          "清湖北",
          "牛湖",
          "白石龙",
          "福民",
          "福田口岸",
          "竹村",
          "红山",
          "茜坑",
          "莲花北",
          "观澜",
          "长湖",
          "龙华",
          "龙胜"
        ],
        "5": [
          "全部",
          "上水径",
          "下水径",
          "临海",
          "五和",
          "兴东",
          "前海湾",
          "前湾",
          "前湾公园",
          "坂田",
          "塘朗",
          "大学城",
          "太安",
          "妈湾",
          "宝华",
          "宝安中心",
          "布吉",
          "布心",
          "怡景",
          "杨美",
          "桂湾",
          "民治",
          "洪浪北",
          "深圳北",
          "灵芝",
          "留仙洞",
          "百鸽笼",
          "翻身",
          "荔湾",
          "西丽",
          "赤湾",
          "铁路公园",
          "长岭陂",
          "长龙",
          "黄贝岭"
        ],
        "6": [
          "全部",
          "上屋",
          "上芬",
          "体育中 心",
          "元芬",
          "光明",
          "光明大街",
          "八卦岭",
          "公明广场",
          "凤凰城",
          "合水口",
          "官田",
          "松岗",
          "松岗公园",
          "梅林关",
          "楼村",
          "深圳北",
          "溪头",
          "科学公园",
          "科学馆",
          "红山",
          "红花山",
          "翰岭",
          "薯田埔",
          "通新岭",
          "银湖",
          "长圳"
        ],
        "7": [
          "全部",
          "上沙",
          "八卦岭",
          "农林",
          "华强北",
          "华强南",
          "华新",
          "太安",
          "安托山",
          "桃源村",
          "沙尾",
          "洪湖",
          "深云",
          "珠光",
          "田贝",
          "皇岗口岸",
          "皇岗村",
          "石厦",
          "福 民",
          "福邻",
          "笋岗",
          "红岭北",
          "茶光",
          "西丽",
          "西丽湖",
          "赤尾",
          "车公庙",
          "黄木岗",
          "龙井"
        ],
        "8": ["全部", "梧桐山南", "沙头角", "海山", "深外高中", "盐田港西", "盐田路"],
        "9": [
          "全部",
          "上梅林",
          "下梅林",
          "下沙",
          "人民南",
          "前湾",
          "南山书城",
          "南油",
          "南油西",
          "向西村",
          "园岭",
          "孖岭",
          "怡海",
          "文锦",
          "景田",
          "梅景",
          "梅村",
          "梦海",
          "泥岗",
          "深圳湾公园",
          "深大南",
          "深湾",
          "粤海门",
          "红岭",
          "红岭 北",
          "红岭南",
          "红树湾南",
          "荔林",
          "车公庙",
          "银湖",
          "香梅",
          "高新南",
          "鹿丹村"
        ],
        "10": [
          "全部",
          "上李朗",
          "五和",
          "光雅园",
          "冬瓜岭",
          "凉帽山",
          "华为",
          "华南城",
          "南坑",
          "双拥街",
          "坂田北",
          "孖岭",
          "岗厦",
          "岗头",
          "平湖",
          "木古",
          "甘坑",
          "福民",
          "福田口岸",
          "禾花",
          "莲花村",
          "贝尔路",
          "雅宝",
          "雪象"
        ],
        "11": [
          "全部",
          "前海湾",
          "南山",
          "后亭",
          "后海",
          "塘尾",
          "宝安",
          "机场",
          "机场北",
          "松岗",
          "桥头",
          "沙井",
          "碧头",
          "碧海湾",
          "福永",
          "福田",
          "红树湾南",
          "车公庙",
          "马安山"
        ]
      };
    });
  }

  // getSubWay

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text('777租房助手'),
        ),
        body: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(children: [
              // 在同一个区间里滑动设置最低价和最高价
              Row(
                children: [
                  Text('价格'),
                  const SizedBox(
                    width: 10,
                  ),
                  // 显示价格
                  Text(priceLow.round().toString()),
                  Expanded(
                    child: RangeSlider(
                      values: RangeValues(priceLow, priceHigh),
                      min: 0,
                      max: 15000,
                      divisions: 100,
                      labels: RangeLabels(
                        priceLow.round().toString(),
                        priceHigh.round().toString(),
                      ),
                      onChanged: (RangeValues values) {
                        setState(() {
                          priceLow = values.start;
                          priceHigh = values.end;
                        });
                      },
                    ),
                  ),
                  Text(priceHigh.round().toString()),
                ],
              ),

              // 地铁线路和地铁站，地铁站跟着线路变化
              Row(
                children: [
                  // 从getLines动态获取线路信息
                  LineDropdown(
                    selectedValue: lineNum,
                    items: lineNums,
                    onChanged: (val) {
                      setState(() => {
                            lineNum = val,
                            subwayStation = '全部',
                            subWayStations = subWayStationsMap[val]!
                          });
                    },
                  ),

                  const SizedBox(height: 10),
                  // futureBuilder处理地铁站数据
                  SubwayStationDropdown(
                      selectSubWay: subwayStation,
                      stations: subWayStations,
                      onChanged: (val) {
                        setState(() => subwayStation = val);
                      }),
                ],
              ),

              // Row(
              //   children: [
              //     const Text('小区'),
              //     const SizedBox(
              //       width: 10,
              //     ),
              //     Expanded(
              //       child: TextField(
              //         onChanged: (value) {
              //           setState(() {
              //             community = value;
              //           });
              //         },
              //       ),
              //     ),
              //   ],
              // ),
              Row(
                children: [
                  // 合租类型
                  const Text('合租类型'),
                  const SizedBox(
                    width: 10,
                  ),
                  DropdownButton<String>(
                    value: rentType,
                    items: rentTypes.map((String value) {
                      return DropdownMenuItem<String>(
                        value: value,
                        child: Text(value),
                      );
                    }).toList(),
                    onChanged: (val) => setState(() {
                      rentType = val!;
                    }),
                  ),
                  const Text('排除中介'),
                  Checkbox(
                    value: agentFilter,
                    onChanged: (value) {
                      setState(() {
                        agentFilter = value!;
                      });
                    },
                  ),
                  // 搜索
                  ElevatedButton(
                    onPressed: () {
                      page = 1;
                      searchPosts();
                    },
                    child: const Text('搜索'),
                  ),
                ],
              ),
              // 显示搜索结果
              Expanded(
                child: ListView.builder(
                    controller: _scrollController,
                    itemCount: posts.length,
                    itemBuilder: (context, index) {
                      // 用card组件展示标题，价格，内容，最后回复时间,不适用ListTile
                      return Card(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            ListTile(
                              title: Text(posts[index]['title']),
                              subtitle: Text(
                                  '${posts[index]['price']?.toStringAsFixed(2)}￥\n${posts[index]['lastReplyTime']}'),
                            ),
                            Padding(
                              padding: const EdgeInsets.all(16.0),
                              // 去除content里的标签
                              child: Html(
                                data: posts[index]['content'],
                              ),
                              // child: Text(
                              //   posts[index]['content']
                              //       .replaceAll(RegExp(r'<[^>]*>'), ''),
                              // ),
                            ),
                          ],
                        ),
                      );
                    }),
              ),
              // 分页
              Row(
                mainAxisAlignment: MainAxisAlignment.center, // 水平方向居中对齐
                children: [
                  // 居中
                  ElevatedButton(
                    onPressed: () {
                      setState(() {
                        if (page > 0) {
                          page = page - 1;
                        }
                        searchPosts();
                      });
                    },
                    child: const Text('上一页'),
                  ),
                  const SizedBox(
                    width: 10,
                  ),
                  Text('第' + page.toString() + '页'),
                  const SizedBox(
                    width: 10,
                  ),
                  ElevatedButton(
                    onPressed: () {
                      setState(() {
                        page = page + 1;
                        searchPosts();
                      });
                    },
                    child: const Text('下一页'),
                  ),
                ],
              ),
            ])));
  }
}

class LineDropdown extends StatelessWidget {
  final String selectedValue;
  final List<String> items;
  final Function(String) onChanged;

  const LineDropdown({
    required this.selectedValue,
    required this.items,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        const Text('地铁线路:'),
        const SizedBox(width: 10),
        DropdownButton<String>(
          value: selectedValue,
          items: items.map((String value) {
            return DropdownMenuItem<String>(
              value: value,
              child: Text(value),
            );
          }).toList(),
          onChanged: (val) => onChanged(val!),
        ),
      ],
    );
  }
}

class SubwayStationDropdown extends StatelessWidget {
  final String selectSubWay;
  final List<String> stations;
  final Function(String) onChanged;

  const SubwayStationDropdown({
    required this.selectSubWay,
    required this.stations,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        const Text('地铁站:'),
        const SizedBox(width: 10),
        DropdownButton<String>(
          value: selectSubWay,
          items: stations.map((String value) {
            return DropdownMenuItem<String>(
              value: value,
              child: Text(value),
            );
          }).toList(),
          onChanged: (val) => onChanged(val!),
        ),
      ],
    );
  }
}

// webviewPage
class WebViewPage extends StatelessWidget {
  final String title;
  final String url;

  const WebViewPage({Key? key, required this.title, required this.url})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(title),
      ),
      body: InAppWebView(
        initialUrlRequest: URLRequest(url: WebUri(url)),
        onReceivedServerTrustAuthRequest: (controller, challenge) async {
          return ServerTrustAuthResponse(
              action: ServerTrustAuthResponseAction.PROCEED);
        },
      ),
    );
  }
}

void main() {
  runApp(
    MaterialApp(
      home: SearchPage(),
    ),
  );
}
