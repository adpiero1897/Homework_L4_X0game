import java.util.Random;
import java.util.Scanner;

public class Homework_l4_Class {

    private static int mapSize;
    private static int winLine;
    private static final Scanner SC = new Scanner(System.in);
    private static char[][] map;
    private static final char cell_X = 'X';
    private static final char cell_0 = 'O';
    private static final char dot = '•';

    //Список полей ниже понадобится при принятии решений компьютером о следующем своем ходе
    private static int x_Last_AI_turn = -1;
    private static int y_Last_AI_turn = -1;
    private static int x_Last_AI_Buffer = -1;
    private static int y_Last_AI_Buffer = -1;
    private static int x_Last_Human_turn;
    private static int y_Last_Human_turn;
    private static int x_AI_Potential_Turn;
    private static int y_AI_Potential_Turn;

    private static int[] x_next_potential_lock = new int[8];//будем записывать потенциальные координаты ходов комп-а
    private static int[] y_next_potential_lock = new int[8] ;//чтобы потом выбрать из них уже следующий ход для него
    private static int[] x_next_potential_forward = new int[8]; //первая двойка - лучшие защитные ходы
    private static int[] y_next_potential_forward = new int[8] ;// нижняя двойка - лучшие атакующие ходы
    private static int Human_Potential_win_line;      //Показывает, насколько игрок близок к победе
    private static int AI_Potential_win_line;     //Показывает, насколько компьютер близок к победе
    private static int index_potential_turns_lock = 0;
    private static int index_potential_turns_forward = 0;

    public static void main(String[] args) {
        HumanSetupMap();
        mapInitialization();
        draw_a_map();

        while (true) {
            humanTurn();
            draw_a_map();
            if (isWin(cell_X, x_Last_Human_turn, y_Last_Human_turn)) {
                System.out.println("Поздравляем! Вы победили!");
                break;
            }
            if (drawChecking()) {
                break;
            }
            computerTurn();
            draw_a_map();
            if (isWin(cell_0, x_Last_AI_turn, y_Last_AI_turn)) {
                System.out.println("К сожалению, вы програли!");
                break;
            }
            if (drawChecking()) {
                break;
            }
        }
    }


    public static void HumanSetupMap() {
        int sizeMap;
        int sizeLine;
        System.out.println("Введите размер поля (сторону квадрата)," +
                " а потом необходимую для победы длину линии в формате \"сторона поля пробел длина \"");
        do {
            sizeMap = -1;
            sizeLine = -1;

            if (SC.hasNextInt()) {
                sizeMap = SC.nextInt();
            }
            if (SC.hasNextInt()) {
                sizeLine = SC.nextInt();
            }
            SC.nextLine();
        } while (!isValidSetupMap(sizeMap, sizeLine));
    }


    public static boolean isValidSetupMap(int sizeMap, int sizeLine) {
        if (sizeMap < 1 || sizeLine < 1 || sizeLine > sizeMap) {
            System.out.println("Вы ввели неправильные параметры. Введите размер поля (сторону квадрата)," +
                    " а потом необходимую для победы длину линии в формате \"сторона поля пробел длина \"");
            return false;
        }
        mapSize = sizeMap;
        winLine = sizeLine;
        return true;
    }


    public static void draw_a_map() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[j][i] + "  ");
            }
            System.out.println();
        }
        System.out.println();
    }


    public static void mapInitialization() {
        map = new char[mapSize][mapSize];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = dot;
            }
        }
    }


    public static boolean drawChecking() {
        for (char[] chars : map) {
            for (char aChar : chars) {
                if (aChar == dot) {
                    return false;
                }
            }
        }
        System.out.println("Ничья! Что поделать...");
        return true;
    }


    public static boolean isWin(char cell, int xLastTurn, int yLastTurn) {
        //Проверяем поочередно линии ← → или ↑ ↓ или диаг. \ или диаг. / , не стали ли они победными (арг. maxDots = 0)
        if(find_Max_Potential_line(1, 0 ,xLastTurn, yLastTurn, cell, 0)>=winLine){
            return true;
        }
        if(find_Max_Potential_line(0, 1 ,xLastTurn, yLastTurn, cell, 0) >=winLine){
            return true;
        }
        if(find_Max_Potential_line(1, 1 ,xLastTurn, yLastTurn, cell, 0) >=winLine){
            return true;
        }
        if(find_Max_Potential_line(1, -1 ,xLastTurn, yLastTurn, cell, 0) >=winLine){
            return true;
        }
        return false;
    }


    public static void humanTurn() {
        int xPoint;
        int yPoint;
        System.out.println("Введите координаты, где поставить Х в формате \"x пробел y\"");
        do {
            xPoint = -1;
            yPoint = -1;

            if (SC.hasNextInt()) {
                xPoint = SC.nextInt();
            }
            if (SC.hasNextInt()) {
                yPoint = SC.nextInt();
            }
            SC.nextLine();
        } while (!isValidHumanTurn(xPoint, yPoint));
    }


    public static boolean isValidHumanTurn(int xPoint, int yPoint) {
        if (xPoint < 1 || yPoint < 1 || xPoint > mapSize || yPoint > mapSize) {
            System.out.println("Некорректные координаты для вашего хода. Введите координаты в формате \"x пробел y\"");
            return false;
        }

        if (map[xPoint - 1][yPoint - 1] == dot) {
            map[xPoint - 1][yPoint - 1] = cell_X;
            x_Last_Human_turn = xPoint - 1;
            y_Last_Human_turn = yPoint - 1;
            return true;
        }
        System.out.println("Некорректные координаты для вашего хода. Введите координаты в формате \"x пробел y\"");
        return false;
    }


    public static void computerTurn() {
        int xPoint = 0;    //Координата X хода компьютера в результате работы метода
        int yPoint = 0;    //Координата Y хода компьютера в результате работы метода
        //блок просмотра компьютером лучших вариантов
        //Сначала смотрим, насколько близок противник (человек) к победе, и ищем ходы для его блокировки
        Human_Potential_win_line = 0;
        index_potential_turns_lock = 0;
        lines_X_Viewer(1, 0); //Направление → проверили
        lines_X_Viewer(-1, 0); //Направление ← проверили
        lines_X_Viewer(0, 1); //Направление ↓ проверили
        lines_X_Viewer(0, -1); //Направление ↑ проверили
        lines_X_Viewer(1, 1); //Направление по \ диагонали ↓ → проверили
        lines_X_Viewer(-1, 1); //Направление по / диагонали ↓ ← проверили
        lines_X_Viewer(1, -1); //Направление по / диагонали ↑ → проверили
        lines_X_Viewer(-1, -1); //Направление по \ диагонали ↑ ← проверили

        if (x_Last_AI_turn >= 0) {  //Если компьютер уже начал серию своих ходов, то можно пытаться играть ему на победу
            int AI_Buffer_Win_Line = AI_Potential_win_line;//Буфер на случай, если от последнего 0 линии атаки стали хуже
            AI_Potential_win_line = 0;
            index_potential_turns_forward=0;
            lines_0_Viewer(1, 0); //Направление → проверили
            lines_0_Viewer(-1, 0); //Направление ← проверили
            lines_0_Viewer(0, 1); //Направление ↑ проверили
            lines_0_Viewer(0, -1); //Направление ↓ проверили
            lines_0_Viewer(1, 1); //Направление по \ диагонали ↓ → проверили
            lines_0_Viewer(-1, 1); //Направление по / диагонали ↓ ← проверили
            lines_0_Viewer(1, -1); //Направление по / диагонали ↑ → проверили
            lines_0_Viewer(-1, -1); //Направление по \ диагонали ↑ ← проверили

            if (AI_Potential_win_line < AI_Buffer_Win_Line) {//на случай, если от последнего 0 линии атаки стали хуже
                x_Last_AI_turn = x_Last_AI_Buffer;
                y_Last_AI_turn = y_Last_AI_Buffer;
                //Считаем возможную атаку от лучшего последнего хода компьютера для потенциальной победной линии
                AI_Potential_win_line = 0;
                index_potential_turns_forward=0;
                lines_0_Viewer(1, 0); //Направление → проверили
                lines_0_Viewer(-1, 0); //Направление ← проверили
                lines_0_Viewer(0, 1); //Направление ↑ проверили
                lines_0_Viewer(0, -1); //Направление ↓ проверили
                lines_0_Viewer(1, 1); //Направление по \ диагонали ↓ → проверили
                lines_0_Viewer(-1, 1); //Направление по / диагонали ↓ ← проверили
                lines_0_Viewer(1, -1); //Направление по / диагонали ↑ → проверили
                lines_0_Viewer(-1, -1); //Направление по \ диагонали ↑ ← проверили

            } else {
                x_Last_AI_Buffer = x_Last_AI_turn;
                y_Last_AI_Buffer = y_Last_AI_turn;
            }

        }

        int[] x_Computer_turns; //Список рассчитаных лучших ходов для компьютера по Оси 0_X
        int[] y_Computer_turns; //Список рассчитаных лучших ходов для компьютера по Оси 0_Y
        boolean coincidence = false;
        if (AI_Potential_win_line < Human_Potential_win_line) {   //компьютеру нужно защищаться
            x_Computer_turns = new int[index_potential_turns_lock];
            y_Computer_turns = new int[index_potential_turns_lock];
            for (int i = 0; i < index_potential_turns_lock; i++) {
                x_Computer_turns[i] = x_next_potential_lock[i];
                y_Computer_turns[i] = y_next_potential_lock[i];
            }
            //Пробуем усилить свои атакующие линии, даже играя в защиту
            if (x_Last_AI_turn >= 0) {  //Если компьютер уже начал серию своих ходов
                for (int i = 0; i < index_potential_turns_lock; i++) {
                    for (int j = 0; j < index_potential_turns_forward; j++) {
                        if (x_next_potential_lock[i] == x_next_potential_forward[j] &&
                                y_next_potential_lock[i] == y_next_potential_forward[j]) {
                            xPoint = x_next_potential_lock[i];
                            yPoint = y_next_potential_lock[i];
                            coincidence = true; //Совпадение атакующих и защитных интересов компьютера. Так и походим!
                            break;
                        }
                    }
                    if (coincidence) {
                        break;
                    }
                }
            }
        } else {    //компьютеру можно атаковать
            x_Computer_turns = new int[index_potential_turns_forward];
            y_Computer_turns = new int[index_potential_turns_forward];
            for (int i = 0; i < index_potential_turns_forward; i++) {
                x_Computer_turns[i] = x_next_potential_forward[i];
                y_Computer_turns[i] = y_next_potential_forward[i];
            }
            //Пробуем, атакуя, усиливать защиту
            for (int i = 0; i < index_potential_turns_forward; i++) {
                for (int j = 0; j < index_potential_turns_lock; j++) {
                    if (x_next_potential_forward[i] == x_next_potential_lock[j] &&
                            y_next_potential_forward[i] == y_next_potential_lock[j]) {
                        xPoint = x_next_potential_forward[i];
                        yPoint = y_next_potential_forward[i];
                        coincidence = true; //Совпадение атакующих и защитных интересов компьютера. Так и походим!
                        break;
                    }
                }
                if (coincidence) {
                    break;
                }
            }
        }
        if (!coincidence) { //Если оптимальную стратегию защиты/нападения не нашли, то найдем тактически оптимальный ход
            //тактическая значимость - вес ячейки (количество возможных линий победы по ней)
            int[] value_Of_Computer_turns = new int[x_Computer_turns.length];
            int Max_value_Of_Computer_turns = 0;
            int Max_Value_Count = 0;
            for (int i = 0; i < x_Computer_turns.length; i++) {
                if (is_Size_A_Potential_Win(1,0,x_Computer_turns[i],y_Computer_turns[i],cell_0)) {   //← →
                    value_Of_Computer_turns[i]++;
                }
                if (is_Size_A_Potential_Win(0,1,x_Computer_turns[i],y_Computer_turns[i],cell_0)) {   //↑ ↓
                    value_Of_Computer_turns[i]++;
                }
                if (is_Size_A_Potential_Win(1,1,x_Computer_turns[i],y_Computer_turns[i],cell_0)) { //диаг. \
                    value_Of_Computer_turns[i]++;
                }
                if (is_Size_A_Potential_Win(1,-1,x_Computer_turns[i],y_Computer_turns[i],cell_0)) {//диаг. /
                    value_Of_Computer_turns[i]++;
                }
                if(value_Of_Computer_turns[i] > Max_value_Of_Computer_turns){
                    Max_value_Of_Computer_turns = value_Of_Computer_turns[i];
                    Max_Value_Count = 1;
                }else if(value_Of_Computer_turns[i] == Max_value_Of_Computer_turns){
                    Max_Value_Count++;
                }
            }
            int[] x_Points_Array = new int[Max_Value_Count];
            int[] y_Points_Array = new int[Max_Value_Count];
            for(int i = 0, j = 0;j < x_Points_Array.length; i++){  //Заполним массив ходов наиболее тактически значимыми
                if (value_Of_Computer_turns[i] == Max_value_Of_Computer_turns){
                    x_Points_Array[j] = x_Computer_turns[i];
                    y_Points_Array[j] = y_Computer_turns[i];
                    j++;
                }
            }
            Random rand = new Random();
            int random_Turn = rand.nextInt(x_Points_Array.length);
            xPoint = x_Points_Array[random_Turn];
            yPoint = y_Points_Array[random_Turn];
        }
        map[xPoint][yPoint] = cell_0;
        x_Last_AI_turn = xPoint;
        y_Last_AI_turn = yPoint;
    }

    public static boolean isValidComputerTurn(int xPoint, int yPoint) {
        if (map[xPoint][yPoint] == dot) {
            map[xPoint][yPoint] = cell_0;
            x_Last_AI_turn = xPoint;
            y_Last_AI_turn = yPoint;
            return true;
        }
        return false;
    }

    //в методе ниже первые 2 аргумента - векторы движения, 2 далее - точка отсчета, последняя - кого проверяем
    //Последний аргумент maxDots = 1 для проверки потенциально сильных линий, maxDots = 0 для проверки на уже победу
    public static int find_Max_Potential_line(int xStep, int yStep,int x0_Point, int y0_Point, char cell,int maxDots) {
        //сначала проверим, можно ли по этой линии, вообще, выиграть
        if(!is_Size_A_Potential_Win(xStep, yStep, x0_Point, y0_Point, cell)){
            return 0; //По этой линии выиграть не получится, даже считать больше нет смысла
        }
        //если дошли до сюда, то значит выиграть по этой линии можно
        int max_Potential_win_Line=0;
        //int maxDots = 1; //Максимальное число ячеек на пути
        for (int x = x0_Point, y = y0_Point; x<mapSize && y < mapSize && x >= 0 && y >= 0; x += xStep, y += yStep,
                max_Potential_win_Line++){
            if(map[x][y] != cell){
                if(map[x][y]==dot){
                    if (maxDots == 1) {
                        max_Potential_win_Line--;
                        maxDots--;
                        x_AI_Potential_Turn = x;
                        y_AI_Potential_Turn = y;
                    }else{
                        break;
                    }
                }else{      //Попали на 0, который уже перекрывает всё движение X далее (или наоборот для 0 на Х попали)
                    break;      //Дошли до границы поля
                }
            }
        }
        //Теперь в обратную сторону пойдем от точки отсчета (но здесь уже не даём право перескакивать через точки)
        //Если нужно будет считать через перескоки в ЭТУ сторону, это сделает следующий подобный метод с обратными step
        xStep *= -1;
        yStep *= -1;
        for (int x = x0_Point + xStep, y = y0_Point + yStep; x < mapSize && y < mapSize && x >= 0 && y >= 0
                && map[x][y] == cell ;x += xStep, y += yStep/*, max_Potential_win_Line++*/){
            max_Potential_win_Line++;
        }
        return max_Potential_win_Line;
    }


    public static boolean is_Size_A_Potential_Win(int xStep, int yStep, int x0_Point, int y0_Point, char cell){
        int max_line_potential_size=0;  //счетчик размера потенциала линии создадим для этой цели
        for(int x = x0_Point, y = y0_Point; x < mapSize && y < mapSize && x >= 0 && y >= 0 && //+условие достаточности линии
                max_line_potential_size < winLine; x += xStep, y += yStep){
            if(map[x][y] != cell && map[x][y] != dot){
                break;
            }
            max_line_potential_size++;
        }
        //+ в обратную сторону можно продолжать линию и считать её потенциал
        xStep *= -1;
        yStep *= -1;
        for (int x = x0_Point + xStep, y = y0_Point + yStep; x<mapSize && y < mapSize && x >= 0 && y >= 0 && //+условие достаточности линии
                max_line_potential_size < winLine; x += xStep, y += yStep){
            if(map[x][y] != cell && map[x][y] != dot){
                break;
            }
            max_line_potential_size++;
        }
        if(max_line_potential_size >= winLine){
            return true; //По этой линии можно выиграть
        }
        return false;
    }

    public static void lines_X_Viewer(int xStep, int yStep) {
        //в методе ниже первые 2 аргумента - векторы движения, 2 далее - точка отсчета, последняя - кого проверяем
        //Последний аргумент maxDots = 1 для проверки потенциально сильных линий, maxDots = 0 для проверки на уже победу
        int buffer = find_Max_Potential_line(xStep, yStep, x_Last_Human_turn, y_Last_Human_turn, cell_X,1);
        if (buffer > Human_Potential_win_line) {//Комп. ищет максимально опасную для себя линию
            //Если нашёл, то сразу записывает и её значение в максимум и потенциальные ходы-ответы на неё
            Human_Potential_win_line = buffer;
            x_next_potential_lock = new int[8];
            y_next_potential_lock = new int[8];
            index_potential_turns_lock=0;
            //Сбросили потенциальные ходы меньших линий, чтобы записать ходы самых больших (ниже первую такую пишем)
            x_next_potential_lock[index_potential_turns_lock] = x_AI_Potential_Turn;
            y_next_potential_lock[index_potential_turns_lock] = y_AI_Potential_Turn;
            index_potential_turns_lock++;
        }else if(buffer == Human_Potential_win_line){
            x_next_potential_lock[index_potential_turns_lock] = x_AI_Potential_Turn;
            y_next_potential_lock[index_potential_turns_lock] = y_AI_Potential_Turn;
            index_potential_turns_lock++;
        }
    }

    public static void lines_0_Viewer(int xStep, int yStep) {
        //в методе ниже первые 2 аргумента - векторы движения, 2 далее - точка отсчета, последняя - кого проверяем
        //Последний аргумент maxDots = 1 для проверки потенциально сильных линий, maxDots = 0 для проверки на уже победу
        int buffer = find_Max_Potential_line(xStep, yStep, x_Last_AI_turn, y_Last_AI_turn, cell_0,1);
        if (buffer > AI_Potential_win_line) {//Комп. ищет максимально успешную для себя линию
            //Если нашёл, то сразу записывает и её значение в максимум и потенциальные ходы-продолжения для неё
            AI_Potential_win_line = buffer;
            x_next_potential_forward = new int[8];
            y_next_potential_forward = new int[8];
            index_potential_turns_forward=0;
            //Сбросили потенциальные ходы меньших линий, чтобы записать ходы самых больших (ниже первую такую пишем)
            x_next_potential_forward[index_potential_turns_forward] = x_AI_Potential_Turn;
            y_next_potential_forward[index_potential_turns_forward] = y_AI_Potential_Turn;
            index_potential_turns_forward++;
        }else if(buffer == AI_Potential_win_line){
            x_next_potential_forward[index_potential_turns_forward] = x_AI_Potential_Turn;
            y_next_potential_forward[index_potential_turns_forward] = y_AI_Potential_Turn;
            index_potential_turns_forward++;
        }
    }


}