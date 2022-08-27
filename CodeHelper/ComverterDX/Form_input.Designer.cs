namespace ComverterDX
{
    partial class Form_input
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form_input));
            this.panel1 = new System.Windows.Forms.Panel();
            this.error_panel = new System.Windows.Forms.Panel();
            this.label3 = new System.Windows.Forms.Label();
            this.button1 = new System.Windows.Forms.Button();
            this.language_keyword_label = new System.Windows.Forms.Label();
            this.ok_but = new System.Windows.Forms.Button();
            this.input_textBox = new System.Windows.Forms.TextBox();
            this.close_but = new System.Windows.Forms.Button();
            this.titleName2 = new System.Windows.Forms.Label();
            this.titleBar2 = new System.Windows.Forms.Panel();
            this.close_but2 = new System.Windows.Forms.Button();
            this.panel1.SuspendLayout();
            this.error_panel.SuspendLayout();
            this.titleBar2.SuspendLayout();
            this.SuspendLayout();
            // 
            // panel1
            // 
            this.panel1.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.panel1.Controls.Add(this.error_panel);
            this.panel1.Controls.Add(this.button1);
            this.panel1.Controls.Add(this.language_keyword_label);
            this.panel1.Controls.Add(this.ok_but);
            this.panel1.Controls.Add(this.input_textBox);
            this.panel1.Location = new System.Drawing.Point(0, 0);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(450, 240);
            this.panel1.TabIndex = 35;
            // 
            // error_panel
            // 
            this.error_panel.BackColor = System.Drawing.Color.Pink;
            this.error_panel.Controls.Add(this.label3);
            this.error_panel.Location = new System.Drawing.Point(15, 111);
            this.error_panel.Name = "error_panel";
            this.error_panel.Size = new System.Drawing.Size(407, 32);
            this.error_panel.TabIndex = 7;
            this.error_panel.Visible = false;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Font = new System.Drawing.Font("Meiryo UI", 9F, System.Drawing.FontStyle.Bold);
            this.label3.ForeColor = System.Drawing.Color.Crimson;
            this.label3.Location = new System.Drawing.Point(8, 8);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(154, 15);
            this.label3.TabIndex = 6;
            this.label3.Text = "Error：入力形式が違います";
            // 
            // button1
            // 
            this.button1.Font = new System.Drawing.Font("Meiryo UI", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(128)));
            this.button1.Location = new System.Drawing.Point(262, 161);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(71, 47);
            this.button1.TabIndex = 5;
            this.button1.Text = "リセット";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.reset_but_Click);
            // 
            // language_keyword_label
            // 
            this.language_keyword_label.AutoSize = true;
            this.language_keyword_label.Font = new System.Drawing.Font("Meiryo UI", 9F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(128)));
            this.language_keyword_label.Location = new System.Drawing.Point(12, 54);
            this.language_keyword_label.Name = "language_keyword_label";
            this.language_keyword_label.Size = new System.Drawing.Size(79, 15);
            this.language_keyword_label.TabIndex = 4;
            this.language_keyword_label.Text = "Python　入力";
            // 
            // ok_but
            // 
            this.ok_but.Font = new System.Drawing.Font("Meiryo UI", 12F);
            this.ok_but.Location = new System.Drawing.Point(350, 161);
            this.ok_but.Margin = new System.Windows.Forms.Padding(2);
            this.ok_but.Name = "ok_but";
            this.ok_but.Size = new System.Drawing.Size(72, 47);
            this.ok_but.TabIndex = 1;
            this.ok_but.Text = "OK";
            this.ok_but.UseVisualStyleBackColor = true;
            this.ok_but.Click += new System.EventHandler(this.ok_but_Click);
            // 
            // input_textBox
            // 
            this.input_textBox.Font = new System.Drawing.Font("Meiryo UI", 9F);
            this.input_textBox.Location = new System.Drawing.Point(14, 82);
            this.input_textBox.Name = "input_textBox";
            this.input_textBox.Size = new System.Drawing.Size(407, 23);
            this.input_textBox.TabIndex = 2;
            // 
            // close_but
            // 
            this.close_but.BackColor = System.Drawing.Color.Transparent;
            this.close_but.FlatAppearance.BorderSize = 0;
            this.close_but.FlatAppearance.MouseOverBackColor = System.Drawing.Color.LightCoral;
            this.close_but.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.close_but.Font = new System.Drawing.Font("Meiryo UI", 14.25F);
            this.close_but.ForeColor = System.Drawing.Color.White;
            this.close_but.Location = new System.Drawing.Point(586, 0);
            this.close_but.Name = "close_but";
            this.close_but.Size = new System.Drawing.Size(30, 30);
            this.close_but.TabIndex = 32;
            this.close_but.Text = "×";
            this.close_but.UseVisualStyleBackColor = false;
            // 
            // titleName2
            // 
            this.titleName2.AutoSize = true;
            this.titleName2.Font = new System.Drawing.Font("Meiryo UI", 9F, System.Drawing.FontStyle.Bold);
            this.titleName2.ForeColor = System.Drawing.Color.White;
            this.titleName2.Location = new System.Drawing.Point(12, 9);
            this.titleName2.Name = "titleName2";
            this.titleName2.Size = new System.Drawing.Size(112, 15);
            this.titleName2.TabIndex = 0;
            this.titleName2.Text = "CodeHelper　詳細";
            // 
            // titleBar2
            // 
            this.titleBar2.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(64)))), ((int)(((byte)(64)))), ((int)(((byte)(64)))));
            this.titleBar2.Controls.Add(this.close_but2);
            this.titleBar2.Controls.Add(this.close_but);
            this.titleBar2.Controls.Add(this.titleName2);
            this.titleBar2.Dock = System.Windows.Forms.DockStyle.Top;
            this.titleBar2.Location = new System.Drawing.Point(0, 0);
            this.titleBar2.Name = "titleBar2";
            this.titleBar2.Size = new System.Drawing.Size(450, 30);
            this.titleBar2.TabIndex = 34;
            this.titleBar2.MouseDown += new System.Windows.Forms.MouseEventHandler(this.titleBar_MouseDown);
            // 
            // close_but2
            // 
            this.close_but2.BackColor = System.Drawing.Color.Transparent;
            this.close_but2.FlatAppearance.BorderSize = 0;
            this.close_but2.FlatAppearance.MouseOverBackColor = System.Drawing.Color.Crimson;
            this.close_but2.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.close_but2.Font = new System.Drawing.Font("Meiryo UI", 14.25F);
            this.close_but2.ForeColor = System.Drawing.Color.White;
            this.close_but2.Location = new System.Drawing.Point(420, 0);
            this.close_but2.Name = "close_but2";
            this.close_but2.Size = new System.Drawing.Size(30, 30);
            this.close_but2.TabIndex = 34;
            this.close_but2.Text = "×";
            this.close_but2.UseVisualStyleBackColor = false;
            this.close_but2.Click += new System.EventHandler(this.close_but_Click);
            // 
            // Form_input
            // 
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.None;
            this.BackColor = System.Drawing.Color.White;
            this.ClientSize = new System.Drawing.Size(450, 240);
            this.Controls.Add(this.titleBar2);
            this.Controls.Add(this.panel1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.None;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Margin = new System.Windows.Forms.Padding(2);
            this.Name = "Form_input";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "Form_input";
            this.Load += new System.EventHandler(this.Form_input_Load);
            this.panel1.ResumeLayout(false);
            this.panel1.PerformLayout();
            this.error_panel.ResumeLayout(false);
            this.error_panel.PerformLayout();
            this.titleBar2.ResumeLayout(false);
            this.titleBar2.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Panel error_panel;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Label language_keyword_label;
        private System.Windows.Forms.Button ok_but;
        private System.Windows.Forms.TextBox input_textBox;
        private System.Windows.Forms.Button close_but;
        private System.Windows.Forms.Label titleName2;
        private System.Windows.Forms.Panel titleBar2;
        private System.Windows.Forms.Button close_but2;
    }
}