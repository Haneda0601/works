using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Data.SqlClient;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Runtime.InteropServices;

namespace ComverterDX
{
    public partial class Form1 : Form
    {
        //クラスの準備
        private SqlConnection cn = new SqlConnection();
        private SqlCommand cmd = new SqlCommand();
        private SqlDataReader rd; 
        private string cnstr =
                @"Data Source = (LocalDB)\MSSQLLocalDB;" +
                @"AttachDbFilename=|DataDirectory|\CDXDatabase.mdf;" +
                @"Integrated Security = True;" +
                "Connect Timeout = 30";

        Form_input f_inp;
        string[] sendData = new string[2];
        List<ComboBox> Clist;
        List<ComboBox> Llist;
        List<CheckBox> Checklist;
        List<Button> DeBut;
        List<Label> ConLabel;

        int NowECount = 1;

        public Form1()
        {
            InitializeComponent();
            f_inp = new Form_input();
            f_inp.f_m = this;
            Clist = new List<ComboBox>() { keyword_combo1, keyword_combo2, keyword_combo3, keyword_combo4, keyword_combo5 };
            Llist = new List<ComboBox>() { language_sel_combo1, language_sel_combo2, language_sel_combo3, language_sel_combo4, language_sel_combo5 };
            Checklist = new List<CheckBox>() { nest_check1, nest_check2, nest_check3, nest_check4 };
            DeBut = new List<Button>() { detail_but1, detail_but2, detail_but3, detail_but4, detail_but5 };
            ConLabel = new List<Label>() { condition_label1, condition_label2, condition_label3, condition_label4, condition_label5 };
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            titlebar.MouseDown += new MouseEventHandler(titleBar_MouseDown);
            AllEFalse(1);
        }

        private void AllEFalse(int n)
        {
            for(int i = n; i < 5; i++)
            {
                if(i > 0) Checklist[i - 1].Enabled = false; 
                Clist[i].Enabled = false;
                Llist[i].Enabled = false;
                DeBut[i].Enabled = false;
            }
        }

        private void ETrue()
        {
            for (int i = 0; i < NowECount; i++)
            {
                if (i > 0) Checklist[i - 1].Enabled = true;
                Clist[i].Enabled = true;
                Llist[i].Enabled = true;
                DeBut[i].Enabled = true;
            }
        }

        public string[] receiveData
        {
            set
            {
                sendData = value;
                ConLabel[int.Parse(sendData[0])].Text = sendData[1];
            }
            get
            {
                return sendData;
            }
        }

        public string[] db(string lang,string key)
        {
            string[] s = new string[6];
            cn.ConnectionString = cnstr;
            cn.Open();
            cmd.Connection = cn;
            cmd.CommandType = CommandType.Text;
            cmd.CommandText = "SELECT * FROM [dbo].[TemplateTable] WHERE language = N'"+ lang +"' AND keyword = N'"+ key +"'";
            rd = cmd.ExecuteReader();
            while (rd.Read())
            {
                s[0] = rd["code"].ToString();
                s[1] = rd["template"].ToString();
                s[2] = rd["rex"].ToString();
                s[3] = rd["language"].ToString();
                s[4] = rd["keyword"].ToString();
            }
            rd.Close();
            cn.Close();
            return s;
        }

        private void output_but_Click(object sender, EventArgs e)
        {
            if (gui_input_radio.Checked)
            {
                try
                {
                    int tbcnt = 0;
                    string[] s = db(Llist[0].Text, Clist[0].Text);
                    string output_temp = s[0].Replace("[t]", "\t").Replace("[t2]", "") + "\r\n";
                    output_temp = template_output(0, output_temp);
                    for (int i = 1; i < NowECount - 1; i++)
                    {
                        s = db(Llist[i].Text, Clist[i].Text);
                        if (s[0] != null)
                        {
                            if ((Clist[i - 1].Text == "分岐" || Clist[i - 1].Text == "反復") && Checklist[i - 1].Checked)
                            {
                                tbcnt++;
                                string[] tb = tbs(tbcnt);
                                output_temp = output_temp.Replace("[*]", s[0]).Replace("[t]", tb[0]).Replace("[t2]", tb[1]) + "\r\n";
                            }
                            else
                            {
                                tbcnt = 0;
                                output_temp += s[0] + "\r\n";
                            }
                        }
                        else
                        {
                            break;
                        }
                        output_temp = template_output(i, output_temp);
                    }
                    output_result_text.Text = output_temp.Replace("[*]", "").Replace("ern", "\r\n").Replace("[t]", "").Replace("[t2]", "");
                }
                catch
                {
                    output_result_text.Text = "GUIまたはコマンドに予約語と言語を入力してください。";
                }
            }
            else
            {
                cmd_output();
            }
        }

        private string template_output(int num,string output)
        {
            string[] aa = ConLabel[num].Text.Split(',');
            int p = 0;

            foreach (var z in aa)
            {
                string[] bb = z.Split(':');
                int i = 0;

                foreach (var y in bb)
                {
                    if (i == 1)
                    {
                        output = output.Replace("[" + p + "]",y);
                    }
                    i += 1;
                }
                p += 1;
            }
            return output;
        }

        private string[] tbs(int c)
        {
            string[] s = { "\t", "" };
            for(int i = 0; i < c; i++)
            {
                s[0] += "\t";
                s[1] += "\t";
            }
            return s;
        }

        private void copy_but_Click(object sender, EventArgs e)
        {
            if(output_result_text.Text != "") Clipboard.SetText(output_result_text.Text);
        }

        private void detail_but1_Click(object sender, EventArgs e)
        {
            F2Show(0);
        }

        private void detail_but2_Click(object sender, EventArgs e)
        {
            F2Show(1);
        }

        private void detail_but3_Click(object sender, EventArgs e)
        {
            F2Show(2);
        }

        private void detail_but4_Click(object sender, EventArgs e)
        {
            F2Show(3);
        }

        private void detail_but5_Click(object sender, EventArgs e)
        {
            F2Show(4);
        }

        private void F2Show(int num)
        {
            string[] s = keylang_str(num);
            if (s != null && s[1] != "")
            {
                s[1] = ConLabel[num].Text;
                s[5] = num.ToString();
                f_inp.SendData = s;
                f_inp.ShowDialog();
            }
            else if (s == null)
            {
                ConLabel[num].Text = "予約語または言語が設定されていない為詳細は開けません。";
            }
            else if(s[1] == "")
            {
                ConLabel[num].Text = "入力する値が無い為詳細は開けません。";
            }
        }

        private void keyword_combo1_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(0);
            condition_label1.Text = (s != null) ? s[1] : "";
            key_combo_check(0);
        }

        private void keyword_combo2_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(1);
            condition_label2.Text = (s != null) ? s[1] : "";
            key_combo_check(1);
        }

        private void keyword_combo3_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(2);
            condition_label3.Text = (s != null) ? s[1] : "";
            key_combo_check(2);
        }

        private void keyword_combo4_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(3);
            condition_label4.Text = (s != null) ? s[1] : "";
            key_combo_check(3);
        }

        private void keyword_combo5_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(4);
            condition_label5.Text = (s != null) ? s[1] : "";
            key_combo_check(4);
        }

        private void language_sel_combo1_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(0);
            condition_label1.Text = (s != null) ? s[1] : "";
        }

        private void language_sel_combo2_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(1);
            condition_label2.Text = (s != null) ? s[1] : "";
        }

        private void language_sel_combo3_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(2);
            condition_label3.Text = (s != null) ? s[1] : "";
        }

        private void language_sel_combo4_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(3);
            condition_label4.Text = (s != null) ? s[1] : "";
        }

        private void language_sel_combo5_SelectionChangeCommitted(object sender, EventArgs e)
        {
            string[] s = keylang_str(4);
            condition_label5.Text = (s != null) ? s[1] : "";
        }

        public string[] keylang_str(int num)
        {
            if (Clist[num].Text != "" && Llist[num].Text != "")
            {
                return db(Llist[num].Text,Clist[num].Text);
            }
            return null;
        }

        private void key_combo_check(int num)
        {
            if(Clist[num].Text == "" && NowECount > 1)
            {
                for(int i = num;i < NowECount-1; i++)
                {
                    Llist[i].Text = Llist[i + 1].Text;
                    Clist[i].Text = Clist[i + 1].Text;
                    ConLabel[i].Text = ConLabel[i + 1].Text;
                    if(i > 0)
                    {
                        Checklist[i - 1].Checked = Checklist[i].Checked;
                    }
                }
                NowECount--;
                Clist[NowECount].Text = "";
                Llist[NowECount].Text = "";
                ConLabel[NowECount].Text = "";
                Checklist[NowECount - 1].Checked = false;
                Checklist[NowECount - 1].Enabled = false;
                Clist[NowECount].Enabled = false;
                Llist[NowECount].Enabled = false;
                DeBut[NowECount].Enabled = false;
            }
            else if(Clist[num].Text != "")
            {
                if (NowECount - 1 == num && NowECount < 5)
                {
                    Checklist[num].Enabled = true;
                    Clist[num + 1].Enabled = true;
                    Llist[num + 1].Enabled = true;
                    DeBut[num + 1].Enabled = true;
                    NowECount++;
                }
            }
        }

        private void mini_but_Click(object sender, EventArgs e)
        {
            WindowState = FormWindowState.Minimized;
        }

        private void close_but_Click(object sender, EventArgs e)
        {
            Close();
        }

        private const int WM_NCLBUTTONDOWN = 0xA1;
        private const int HT_CAPTION = 0x2;

        [DllImport("user32.dll", CharSet = CharSet.Auto)]
        private static extern IntPtr SendMessage(
            IntPtr hWnd, int Msg, IntPtr wParam, IntPtr lParam);
        [DllImportAttribute("user32.dll")]
        private static extern bool ReleaseCapture();


        //Form1のMouseDownイベントハンドラ
        private void titleBar_MouseDown(object sender, MouseEventArgs e)
        {
            if (e.Button == MouseButtons.Left)
            {
                //マウスのキャプチャを解除
                ReleaseCapture();
                //タイトルバーでマウスの左ボタンが押されたことにする
                SendMessage(Handle, WM_NCLBUTTONDOWN, (IntPtr)HT_CAPTION, IntPtr.Zero);
            }
        }

        private void cmd_output()
        {
            string t = cmd_textbox.Text + " >>";
            string[] s = t.Split(' ');
            Dictionary<string, string> kldic = new Dictionary<string, string>() { { "py", "1:Python" }, { "c", "1:C" }, { "ja", "1:Java" }, { "cp", "1:C++" }, { "cs", "1:C#" }, { "i", "2:入力" }, { "o", "2:出力" }, { "if", "2:分岐" }, { "f", "2:反復" }, { "n", "3:nest" }, { ">>", "4:" }, { "help", "5:" } };

            int scn = 0, c = 0;
            string[] tmp = new string[3];
            string[,] result_temp = new string[5, 3];
            foreach (string temp in s)
            {
                if (temp == "help") scn = 2;
                try
                {
                    string[] s2 = kldic[temp].Split(':');
                    if (int.Parse(s2[0]) <= 2)
                    {
                        if (tmp[int.Parse(s2[0]) - 1] == null)
                        {
                            tmp[int.Parse(s2[0]) - 1] = s2[1];
                            scn++;
                        }
                        else
                        {
                            // keyword・languageがどちらか二つ以上ある場合エラー
                            output_result_text.Text = "Error:予約語・言語の指定が多いです。";
                            return;
                        }
                    }
                    else
                    {
                        if (scn >= 2)
                        {
                            switch (int.Parse(s2[0]))
                            {
                                case 3: // nest
                                    tmp[2] = "Nest";
                                    scn++;
                                    break;
                                case 4: // >>
                                    result_temp[c, 0] = tmp[0];
                                    result_temp[c, 1] = tmp[1];
                                    result_temp[c, 2] = tmp[2];
                                    tmp = new string[3];
                                    scn = 0;
                                    c++;
                                    break;
                                case 5:// help
                                    output_result_text.Text = "コマンド記法\r\n１つ　　：[言語] [予約語]\r\n２つ以上：[言語] [予約語] >> [言語] [予約語] (無し)|[ネスト] ・・・\r\n\r\n[言語]   ：ja, py, c, cp, cs (Java, Python, C, C++, C#)\r\n[予約語] ：i, o, if, f（入力, 出力, 分岐, 反復）\r\n[ネスト] ：n";
                                    return;
                            }
                        }
                        else
                        {
                            // keyword・languageの二つを取得する前に">>"だとエラー
                            output_result_text.Text = "Error:予約語・言語を正しく指定して下さい。";
                            return;
                        }
                    }
                }
                catch
                {
                    output_result_text.Text = "Error:適切な文字を入力して下さい。";
                }
            }
            sub_output(result_temp, c);
        }

        private void sub_output(string[,] st,int cnt)
        {
            int tbcnt = 0;
            string[] s = db(st[0,0], st[0,1]);
            string output_temp = s[0].Replace("[t]", "\t").Replace("[t2]", "") + "\r\n";
            output_temp = sub_template_output(output_temp,s[1]);
            for (int i = 1; i < cnt; i++)
            {
                s = db(st[i, 0], st[i, 1]);
                if (s[0] != null)
                {
                    if ((st[i - 1, 1] == "分岐" || st[i - 1, 1] == "反復") && st[i,2] == "Nest")
                    {
                        tbcnt++;
                        string[] tb = tbs(tbcnt);
                        output_temp = output_temp.Replace("[*]", s[0]).Replace("[t]", tb[0]).Replace("[t2]", tb[1]) + "\r\n";
                    }
                    else
                    {
                        tbcnt = 0;
                        output_temp += s[0] + "\r\n";
                    }
                }
                else
                {
                    break;
                }
                output_temp = sub_template_output(output_temp,s[1]);
            }
            output_result_text.Text = output_temp.Replace("[*]", "").Replace("ern", "\r\n").Replace("[t]", "").Replace("[t2]", "");
        }

        private string sub_template_output(string output,string temp)
        {
            string[] aa = temp.Split(',');
            int p = 0;

            foreach (var z in aa)
            {
                string[] bb = z.Split(':');
                int i = 0;

                foreach (var y in bb)
                {
                    if (i == 1)
                    {
                        output = output.Replace("[" + p + "]", y);
                    }
                    i += 1;
                }
                p += 1;
            }
            return output;
        }

        private void gui_input_radio_CheckedChanged(object sender, EventArgs e)
        {
            if (gui_input_radio.Checked)
            {
                ETrue();
                cmd_textbox.Text = "\"help\"と入力でコマンド参照";
                cmd_textbox.Enabled = false;
            }
            else
            {
                AllEFalse(0);
                cmd_textbox.Enabled = true;
            }
        }

        private void cmd_textbox_Click(object sender, EventArgs e)
        {
            if(cmd_textbox.Text == "\"help\"と入力でコマンド参照") cmd_textbox.Text = "";
        }
    }
}
