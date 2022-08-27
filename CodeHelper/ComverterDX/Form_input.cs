using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Text.RegularExpressions;
using System.Data.SqlClient;
using System.Runtime.InteropServices;

namespace ComverterDX
{
    public partial class Form_input : Form
    {
        private SqlConnection cn = new SqlConnection();
        private SqlCommand cmd = new SqlCommand();
        private string cnstr =
                @"Data Source = (LocalDB)\MSSQLLocalDB;" +
                @"AttachDbFilename=|DataDirectory|\CDXDatabase.mdf;" +
                @"Integrated Security = True;" +
                "Connect Timeout = 30";

        public Form1 f_m;
        string[] sendData = new string[6];
        public Form_input()
        {
            InitializeComponent();
        }

        private void Form_input_Load(object sender, EventArgs e)
        {
            language_keyword_label.Text = sendData[3] + " : " + sendData[4];
        }

        public string[] SendData
        {
            set
            {
                sendData = value;
                input_textBox.Text = sendData[1];
            }
            get
            {
                return sendData;
            }
        }

        private void ok_but_Click(object sender, EventArgs e)
        {
            if (f_m != null)
            {
                if (Regex.IsMatch(input_textBox.Text, sendData[2]))
                {
                    insert_db(input_textBox.Text);
                    f_m.receiveData = new string[2]{sendData[5], input_textBox.Text};
                    error_panel.Visible = false;
                    Close();
                }
                else
                {
                    error_panel.Visible = true;
                }
            }
        }

        private void reset_but_Click(object sender, EventArgs e)
        {
            input_textBox.Text = sendData[1];
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

        public void insert_db(string temp)
        {
            cn.ConnectionString = cnstr;
            cn.Open();
            cmd.Connection = cn;
            cmd.CommandType = CommandType.Text;
            cmd.CommandText = "UPDATE [dbo].[TemplateTable] SET template = N'" + temp + "' WHERE language = N'" + sendData[3] + "' AND keyword = N'" + sendData[4] + "'";
            cmd.ExecuteNonQuery();
            cn.Close();
        }
    }
}
