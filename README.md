# Compiler
这是一个语法分析框架和基于这个框架开发的类C语言编译器，核心在于语法分析，语义分析和这两部分的优化，其中使用的算法来自编译原理（龙书）。
首先用户写好转换关系，然后对每一个非终结符创建一个和其名字相同的Java类，并为每一个动作编写相同名称的方法，然后语法分析框架会先根据转换关系生成dfa和LALR语法分析表，再通过Java的反射机制实例化语法树并加入动作节点，之后遍历语法树开始中间代码生成，在这个过程中加入回填机制减少生成的goto语句，同时设计回填机制的变种解决了C语言使用函数前必须声明的问题。最后生成中间代码和目标代码中间使用了DAG优化。
这个语法分析框架融合了LR(0),LR(1),LALR语法，首先解析用户的规则文件，使用LR(0)生成dfa（这一步使用LR(0)代替LR(1)可以减少dfa状态数，粗略计算可以从O(n^2)减少至O(n)）。然后构造LR(1) item并求得closure，之后使用LALR生成table。
项目内部按照算法分组，主要是因为一开始只是实现了算法部分，并没用想做这个框架，最后做着做着发现可以往这方面发展，就做出来了（这应该是最不合理的地方了）。这中间有很多次重新设计，为了能把这一堆算法协调起来还是废了好大的劲，这个项目大概用掉了大三下半学期的一大半时间，中间删掉的代码估计比现有的还多，没学过软件工程的坏处呀。做这个东西的时候基本是摸石头过河的过程，其中很多算法在实际运用时会有很大不同，只能自己一点一点摸索。
当时没学过设计模式，凭自己的直觉居然搞出了几个很相似的东西，但是由于路子比较野，代码看着也有些混乱（笑）。