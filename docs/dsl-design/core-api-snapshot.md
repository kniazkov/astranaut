# Актуальный срез базового API ASTranaut

Состояние согласованных решений задачи #19 на 3 ноября 2026 года. Это проект API, а не утверждение о реализации в astranaut-core. История обсуждения: [журнал](issue-19-decisions.md). Алгоритмы: [отдельный план](core-algorithms-review-plan.md).

Документ содержит действующие решения без отменённых альтернатив. Java-фрагменты показывают контракты; внутренние алгоритмы и размещение классов не закрепляются, если это явно отмечено.

## 1. Слоты и неизменяемость

Узел содержит конечный упорядоченный список логических слотов. Содержимое каждого слота — неизменяемый список Node без null. Отдельного объекта Slot в API нет.

| Cardinality | Число детей в готовом узле |
| --- | --- |
| REQUIRED | ровно 1 |
| OPTIONAL | 0 или 1 |
| REPEATED | 0 или более |

Пустые слоты сохраняются. Addition(Expression, Expression) имеет два обязательных слота, даже если их дескрипторы равны. У Parts(Expression*, Expression*) распределения [A], [B, C] и [A, B], [C] структурно различаются.

Списки — форма общего доступа, не обязательный способ хранения. Генератор может хранить обязательного ребёнка в final Expression, необязательного — в nullable-поле, повторяющихся — в неизменяемой коллекции. Наружу null вместо ребёнка не выдаётся. Неизменяемые поддеревья можно разделять между несколькими местами без клонирования.

## 2. Node

```java
public interface Node {
    Type getType();
    String getData();
    List<Node> getChildren(int slotIndex);

    default Fragment getFragment() {
        return EmptyFragment.INSTANCE;
    }

    default Map<String, String> getProperties() {
        return getType().getProperties();
    }

    default String getTypeName() {
        return getType().getName();
    }

    default boolean isSubtypeOf(AbstractType type) {
        return getType().isSubtypeOf(type);
    }

    default List<Node> getChildren(String tag) {
        int index = getType().getSlotIndex(tag);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown slot: " + tag);
        }
        return getChildren(index);
    }

    default List<Node> getAllChildren() {
        List<Node> result = new ArrayList<>();
        for (int index = 0; index < getType().getSlots().size(); index++) {
            result.addAll(getChildren(index));
        }
        return List.copyOf(result);
    }

    default boolean deepCompare(Node other) {
        return NodeStructure.equal(this, other);
    }

    @Override
    boolean equals(Object other);

    @Override
    int hashCode();
}
```

NodeStructure.equal в этом эскизе должен реализовывать новый контракт по слотам; существующую плоскую реализацию нельзя переносить без адаптации. Default-методы генератор может оптимизировать с сохранением семантики.

getData() возвращает сериализованные данные datatype, никогда null; у типа без данных — "". Нативное значение может храниться без дополнительного строкового поля.

getChildren(int) адресует слот в Type.getSlots(), а не ребёнка в плоском списке. Неверный индекс — IndexOutOfBoundsException. Неизвестный тег или null в строковом getter — IllegalArgumentException. Известный пустой слот возвращает пустой список.

getAllChildren() возвращает только непосредственных детей: по порядку слотов, внутри каждого — по порядку детей. Границы слотов в этом списке теряются; он не заменяет представление для сериализации.

Обычный узел получает свойства из типа. Дополнительные свойства относятся к отдельному механизму обёрток, пересмотр которого отложен. getFragment() никогда не null; явно переданный строителю Fragment сохраняется.

Не входят в новый API: deepClone, getLocalHash, getChildCount, плоский getChild(int), getChild(String), getChildTag, getChildrenList, getIteratorOverChildren, forEachChild, статический Node.toString(Node), отдельный getStructuralHash.

## 3. Равенство и хэш Node

Равны типы, сериализованные данные, число детей каждого слота и попарно структуры детей в порядке слотов. Fragment, свойства и Java-класс реализации не участвуют. Общее поддерево и отдельные структурно равные копии дают одинаковый результат.

deepCompare(null) — false. equals(null) и equals объекта, не являющегося Node, — false. Разные хэши позволяют сразу вернуть false; одинаковые требуют проверки структуры. deepCompare не должен делегировать обратно equals того же узла.

Структурный хэш вычисляется при создании узла из уже готовых хэшей детей. hashCode() возвращает готовое значение за O(1); интерфейс не диктует поле хранения.

```text
h = 31 * type.hashCode() + data.hashCode()
h = 31 * h + число_слотов
для каждого слота по порядку:
    h = 31 * h + размер_слота
    для каждого ребёнка по порядку:
        h = 31 * h + child.hashCode()
```

Арифметика Java int с обычным переполнением. Даже пустой слот добавляет размер 0. Коллизии допустимы; равные структуры обязаны иметь одинаковый хэш. Default equals/hashCode в Java-интерфейсе невозможны: реализацию обеспечивает класс узла или общий базовый класс.

## 4. Builder

```java
public interface Builder {
    Type getType();
    void setFragment(Fragment fragment);
    boolean setData(String data);
    boolean setChildren(int slotIndex, List<? extends Node> children);
    boolean setChildren(String tag, List<? extends Node> children);

    default boolean setChild(int slotIndex, Node child) {
        return child != null && setChildren(slotIndex, List.of(child));
    }

    default boolean setChild(String tag, Node child) {
        return child != null && setChildren(tag, List.of(child));
    }

    boolean isValid();
    Node build() throws ConstructionException;
}
```

Тип строителя фиксирован; хранить его отдельным полем необязательно. Геттеров содержимого, методов свойств, плоского setChildren(list) и отдельных clear/remove нет.

Начальное состояние: все слоты пусты, Fragment — EmptyFragment.INSTANCE, данные — default datatype. Если default не проходит validate или обязательные слоты пусты, строитель существует, но невалиден.

setChildren полностью заменяет один слот и сразу фиксирует снимок входного списка. Неверный адрес, несовместимый тип, превышение максимальной кратности, null списка/тега/элемента — false без изменения состояния. Пустой список разрешён даже для REQUIRED; такой строитель становится невалидным. setChild заменяет весь слот одним ребёнком; null даёт false.

setData разбирает и проверяет сериализованные данные. Ожидаемый отказ — false без изменений. null — false. Пустая строка проверяется как обычный ввод; без datatype допустима только "". Строитель не обязан хранить строку вместо нативного значения.

setFragment(null) бросает IllegalArgumentException без изменения состояния. Любой корректный ненулевой Fragment принимается.

build() при невалидности бросает ConstructionException; при нехватке обязательного содержимого сообщение указывает слот. Успех и отказ не меняют строитель. Его можно использовать повторно; уже созданные узлы от последующих изменений не зависят.

## 5. AbstractType и Type

Обязательные методы AbstractType:

```java
String getName();
Set<AbstractType> getBaseTypes();
Set<NamedSlotDescriptor> getNamedSlots();
```

Default-операции AbstractType:

| Метод | Контракт |
| --- | --- |
| Map<String, String> getProperties() | Map.of() по умолчанию |
| boolean isSubtypeOf(AbstractType other) | сам тип и транзитивные предки |
| AbstractType findCommonBaseType(AbstractType other) | общий применимый тип, без Optional |
| Optional<NamedSlotDescriptor> getNamedSlot(String tag) | поиск по полному набору |

Конкретный Type extends AbstractType добавляет:

```java
List<SlotDescriptor> getSlots();
Builder createBuilder();
```

И default int getSlotIndex(String tag): индекс в полном списке, -1 для отсутствующего ненулевого тега. Поиск по именованному набору и поиск индекса могут быть оптимизированы генератором.

getNamedSlot возвращает Optional.empty() для отсутствующего тега. Пустая строка не соответствует никакому тегу. Оба поиска отвергают null с IllegalArgumentException. Отношения типов также отвергают null с IllegalArgumentException.

Все коллекции неизменяемы, ненулевые, без null-элементов; карты без null-ключей и значений. Схемы и родители предоставляются явно, без default пустой схемы или default отсутствия родителей.

Каждый тип содержит свою полную схему. AbstractType предоставляет неупорядоченный набор всех именованных требований. Type дополнительно предоставляет упорядоченный список всех слотов, включая безымянные. Именованная часть списка точно соответствует набору по тегу, типу ребёнка и кратности. Теги не повторяются.

Потомок сохраняет теги родителей и кратность, может уточнять тип ребёнка до подтипа и добавлять слоты. Это включение требований, а не буквальное Set.containsAll после уточнения типов. Свойства типов не наследуются.

getBaseTypes — только непосредственные родители. Граф конечный, без циклов, с единственным корнем Node. Только корень имеет пустой набор; у всех остальных родители явно заданы, все пути заканчиваются корнем.

findCommonBaseType рассматривает сам тип вместе с предками. Единственный наиболее конкретный общий кандидат возвращается. При нескольких несравнимых кандидатах поиск повторяется для всех кандидатов одновременно выше по графу, вплоть до Node. Произвольный выбор и зависимость от порядка попарной свёртки исключены.

В пределах одной DSL имя уникально и определяет равенство типа; hashCode согласован. Одинаковые имена требуют согласованных определений. Сравнение независимых DSL с совпадающими именами не получило отдельного контракта.

createBuilder() каждый раз возвращает новый независимый строитель данного конкретного типа.

## 6. Дескрипторы и SlotSchema

Два отдельных неизменяемых класса, не records, без публичного создания произвольных дескрипторов:

| Метод | NamedSlotDescriptor | SlotDescriptor |
| --- | --- | --- |
| getTag() | String, непустой | Optional<String> |
| getType() | AbstractType | AbstractType |
| getCardinality() | Cardinality | Cardinality |

equals/hashCode внутри одного класса — по трём полям. Между классами равенства нет. Одинаковые безымянные дескрипторы могут занимать разные позиции списка. String и Optional<String> в getTag не допускают прямого наследования этих классов друг от друга.

Согласованный публичный API схемы (перечень сигнатур, не реализация класса):

```java
// SlotSchema
static SlotSchema empty();
static SlotSchema.Constructor create();
static SlotSchema.Constructor create(Set<NamedSlotDescriptor> descriptors);
Set<NamedSlotDescriptor> getNamedSlots();
List<SlotDescriptor> getSlots();

// SlotSchema.Constructor
Constructor required(AbstractType type);
Constructor required(AbstractType type, String tag);
Constructor optional(AbstractType type);
Constructor optional(AbstractType type, String tag);
Constructor repeated(AbstractType type);
Constructor repeated(AbstractType type, String tag);
SlotSchema build();
```

Все добавления возвращают строитель. null типа, null/пустой тег в именованной перегрузке и повторный тег немедленно вызывают IllegalArgumentException без изменения состояния. Безымянные слоты могут повторяться.

build допускает пустую схему, не меняет строитель; результаты неизменяемы и независимы от дальнейших добавлений. empty() сразу возвращает пустую схему; общий экземпляр допустим.

Импорт набора фиксирует снимок, сортирует по естественному порядку String-тегов; последующие добавления идут в конец. null набора/элемента и повтор тега отклоняются с IllegalArgumentException. Пустой набор допустим.

Один строитель используется и для абстрактных типов (только именованные объявления), и для конкретных. Локальная корректность — ответственность строителя; наследование и ограничения объявлений DSL проверяет компилятор.

## 7. Factory

```java
public interface Factory {
    Optional<AbstractType> getAbstractType(String name);

    default Optional<Type> getType(String name) {
        return getAbstractType(name)
            .filter(Type.class::isInstance)
            .map(Type.class::cast);
    }

    default Optional<Builder> createBuilder(String name) {
        return getType(name).map(Type::createBuilder);
    }
}
```

Общий поиск находит и абстрактные, и конкретные типы. Конкретный поиск и создание строителя дают пустой Optional для абстрактного или неизвестного имени. Все результаты Optional ненулевые; null имени — IllegalArgumentException.

Каждый успешный createBuilder даёт новый независимый строитель. Автоматической подстановки DraftNode нет. Пользовательские фабрики выбирают способ получения полноценного типа, соблюдая общий контракт.

## 8. Source, Position, Fragment

Состав основных методов:

```java
// Source
String getFragmentAsString(Position begin, Position end);
default String getFileName() { return ""; }

// Position extends Comparable<Position>
Source getSource();
int getRow();
int getColumn();
// default compareTo(Position other)

// Fragment
Position getBegin();
Position getEnd();
// default String getCode()
// default String getPositionAsString()
```

Source — неизменяемый снимок: изменение файла на диске не меняет его текст. Имя/путь файла служит диагностике, при отсутствии — ""; null запрещён. Имя не определяет идентичность.

Строки и столбцы с 1; столбцы в кодовых единицах UTF-16, табуляция — одна единица. LF начинает следующую строку, CR сам строку не переключает. CRLF и весь исходный текст сохраняются без нормализации. Границы внутри суррогатной пары допустимы; целые code points обеспечивает соответствующий парсер.

| Текст | Позиция конца |
| --- | --- |
| "" | (1, 1) |
| "abc" | (1, 4) |
| "abc\n" | (2, 1) |
| "a😀b" | (1, 5) |

После LF позиция записывается только началом следующей строки. Каждому смещению соответствует одна каноническая пара координат.

Position равны по идентичности Source, строке и столбцу; hashCode согласован. compareTo сравнивает строку, затем столбец; разные источники — IllegalArgumentException.

Обычный Fragment — неизменяемый диапазон [begin, end) одного Source. Позиции ненулевые и допустимые, начало не позже конца; нарушение — IllegalArgumentException. Конструктор границы не переставляет. Равенство — по идентичности источника и координатам двух границ, hashCode согласован.

Source.getFragmentAsString выполняет те же проверки, причём обе позиции принадлежат именно этому Source. Не обрезает и не переставляет диапазон. Равные допустимые позиции дают "". Fragment.getCode возвращает точный текст диапазона.

EmptyFragment.INSTANCE обозначает отсутствие происхождения, не диапазон нулевой длины. Его обе границы — служебная позиция (0, 0) со служебным источником, единственное исключение из правил обычных координат. Она запрещена в конструкторе обычного Fragment. getCode — "".

Диагностический getPositionAsString: "file.c, 2.3-4.5", без имени — "2.3-4.5", для EmptyFragment — "∅".

Хелперы Fragment.fromPositions, Fragment.fromNodes и Position.bounds сохраняются; подробные контракты отложены в план алгоритмов.

## 9. Проверка стыков и оставшиеся вопросы

Проверено согласование адресации Node/Builder с полной схемой Type, двух представлений схемы, Factory с разделением типов, структурного равенства с хэшем, источников и пустого происхождения. Изложение исключает отменённые варианты журнала.

Открытые стыки, которые нельзя молча считать решёнными:

1. Физическое размещение классов дескрипторов и доступ SlotSchema.Constructor к их закрытым конструкторам. Отдельный внешний класс не может вызвать чужой private-конструктор; нужен выбор размещения или внутреннего механизма создания.
2. Инициализация рекурсивных схем типов: слот типа может ссылаться на тот же тип или на тип, ссылающийся обратно. Ацикличность наследования не запрещает такую структуру слотов. Нужно обеспечить полные неизменяемые схемы без чтения частично инициализированных дескрипторов.
3. Формула хэша типа пока требует только согласованности с равенством. Воспроизводимость численного хэша дерева между запусками не обещана.
4. Самостоятельное создание и валидация Position, конкретные конструкторы Source/Fragment и механизм проверки координат через Source требуют проработки реализации; нельзя выводить новые публичные методы из текста этой сводки.
5. Обёртки дополнительных свойств, алгоритмы диапазонов и прочие алгоритмы — отдельный план. Сериализация, восстановление метаданных и остальные вопросы задачи #19 не закрыты.

Это документальная сверка API. Старые Java-тесты core не подтверждают реализацию нового контракта; Java-код в этом PR не менялся.
