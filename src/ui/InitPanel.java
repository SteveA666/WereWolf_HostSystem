package ui;

import core.Player;
import core.Role;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * InitPanel collects player names and auto-assigns roles on confirm.
 * It supports partial games (some roles may be omitted).
 *
 * UI policy: this panel only builds a List<Player>; it does not create GameState.
 */
public class InitPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public interface InitListener {
        void onGameInitialized(List<Player> players);
    }

    private final Random rng;

    private InitListener listener;

    // Top controls
    private JSpinner playerCountSpinner;
    private JButton rebuildButton;

    // Role config controls
    private JSpinner wolvesSpinner; // includes standard werewolves; alpha handled separately
    private JCheckBox alphaWolfBox;
    private JCheckBox witchBox;
    private JCheckBox guardBox;
    private JCheckBox fortuneTellerBox;
    private JCheckBox hunterBox;
    private JCheckBox knightBox;

    // Name inputs
    private JPanel namesPanel;
    private JScrollPane namesScroll;
    private final List<JTextField> nameFields;

    // Confirm
    private JButton confirmButton;

    // Result (available after confirm)
    private List<Player> createdPlayers;

    public InitPanel() {
        super(new BorderLayout());
        this.rng = new Random();
        this.nameFields = new ArrayList<JTextField>();
        this.createdPlayers = null;

        buildHeader();
        buildCenter();
        buildFooter();

        rebuildNameFields(getPlayerCount());
    }

    public void setInitListener(InitListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the most recently created players (null if not confirmed yet).
     */
    public List<Player> getCreatedPlayers() {
        return createdPlayers;
    }

    // UI Construction

    private void buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Game Setup");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel row1 = new JPanel();
        JLabel countLabel = new JLabel("Number of players:");
        playerCountSpinner = new JSpinner(new SpinnerNumberModel(8, 3, 24, 1));
        rebuildButton = new JButton("Apply");

        rebuildButton.addActionListener(new RebuildListener());

        row1.add(countLabel);
        row1.add(playerCountSpinner);
        row1.add(rebuildButton);

        header.add(row1);
        header.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel roles = buildRoleConfigPanel();
        header.add(roles);

        this.add(header, BorderLayout.NORTH);
    }

    private JPanel buildRoleConfigPanel() {
        JPanel rolesPanel = new JPanel(new GridLayout(0, 2, 10, 6));
        rolesPanel.setBorder(BorderFactory.createTitledBorder("Role Options (can be partial)"));

        int n = getPlayerCount();
        int suggestedWolves = suggestWolves(n);

        wolvesSpinner = new JSpinner(new SpinnerNumberModel(suggestedWolves, 0, Math.max(0, n), 1));

        alphaWolfBox = new JCheckBox("Include Alpha Wolf (replaces 1 wolf)");
        witchBox = new JCheckBox("Include Witch");
        guardBox = new JCheckBox("Include Guard");
        fortuneTellerBox = new JCheckBox("Include Fortune Teller");
        hunterBox = new JCheckBox("Include Hunter");
        knightBox = new JCheckBox("Include Knight");

        // Defaults. User can turn them off or on
        alphaWolfBox.setSelected(false);
        witchBox.setSelected(true);
        guardBox.setSelected(true);
        fortuneTellerBox.setSelected(true);
        hunterBox.setSelected(false);
        knightBox.setSelected(false);

        rolesPanel.add(new JLabel("Werewolves count:"));
        rolesPanel.add(wolvesSpinner);

        rolesPanel.add(alphaWolfBox);
        rolesPanel.add(new JLabel(""));

        rolesPanel.add(witchBox);
        rolesPanel.add(guardBox);

        rolesPanel.add(fortuneTellerBox);
        rolesPanel.add(hunterBox);

        rolesPanel.add(knightBox);
        rolesPanel.add(new JLabel(""));

        return rolesPanel;
    }

    // Build player panel
    private void buildCenter() {
        namesPanel = new JPanel();
        namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
        namesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        namesScroll = new JScrollPane(namesPanel);
        namesScroll.setBorder(BorderFactory.createTitledBorder("Player Names"));
        namesScroll.setPreferredSize(new Dimension(600, 400));

        this.add(namesScroll, BorderLayout.CENTER);
    }

    private void buildFooter() {
        JPanel footer = new JPanel();
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        confirmButton = new JButton("Confirm & Assign Roles");
        confirmButton.addActionListener(new ConfirmListener());

        footer.add(confirmButton);
        this.add(footer, BorderLayout.SOUTH);
    }

    // Name fields

    private int getPlayerCount() {
        Object val = playerCountSpinner.getValue();
        if (val instanceof Integer) {
            return ((Integer) val).intValue();
        }
        return 8;
    }

    private void rebuildNameFields(int count) {
        nameFields.clear();
        namesPanel.removeAll();

        for (int i = 0; i < count; i++) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            JLabel label = new JLabel("Player " + (i + 1) + ":");
            JTextField field = new JTextField();
            field.setPreferredSize(new Dimension(300, 28));

            row.add(label, BorderLayout.WEST);
            row.add(field, BorderLayout.CENTER);

            namesPanel.add(row);
            namesPanel.add(Box.createRigidArea(new Dimension(0, 6)));

            nameFields.add(field);
        }

        namesPanel.revalidate();
        namesPanel.repaint();

        // Update wolves spinner max & suggestion when player count changes
        int suggested = suggestWolves(count);
        SpinnerNumberModel model = new SpinnerNumberModel(suggested, 0, Math.max(0, count), 1);
        wolvesSpinner.setModel(model);
    }
    

    private int suggestWolves(int n) {
        // Reasonable default: about 1/4 wolves, minimum 1 if n >= 4, but allow 0 for tiny/partial games.
        if (n <= 3) {
            return 0;
        }
        int s = n / 4;
        if (s < 1) {
            s = 1;
        }
        return s;
    }

    // Get Player Names
    private List<String> collectNamesOrShowError() {
        List<String> names = new ArrayList<String>();
        List<Integer> blanks = new ArrayList<Integer>();

        for (int i = 0; i < nameFields.size(); i++) {
            String raw = nameFields.get(i).getText();
            String name = raw == null ? "" : raw.trim();

            if (name.isEmpty()) {
                blanks.add(Integer.valueOf(i));
                names.add(""); // placeholder for now
            } else {
                names.add(name);
            }
        }

        if (!blanks.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Some player names are empty.\n"
                            + "Would you like to use default names instead?",
                    "Missing Names",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                return null;
            }

            // Fill in default names: "N" for player number N
            for (int i = 0; i < blanks.size(); i++) {
                int idx = blanks.get(i).intValue();
                names.set(idx, String.valueOf(idx + 1));
            }
        }

        return names;
    }


    private List<Role> buildRolePool(int playerCount) {
        List<Role> pool = new ArrayList<Role>();

        int wolves = ((Integer) wolvesSpinner.getValue()).intValue();
        boolean includeAlpha = alphaWolfBox.isSelected();

        // Clamp wolves to playerCount
        if (wolves > playerCount) {
            wolves = playerCount;
        }
        if (wolves < 0) {
            wolves = 0;
        }

        // Add wolves
        for (int i = 0; i < wolves; i++) {
            pool.add(Role.WEREWOLF);
        }

        if (includeAlpha) {
            // Alpha replaces one standard wolf if any exist; otherwise just add alpha
            int idx = indexOfFirst(pool, Role.WEREWOLF);
            if (idx >= 0) {
                pool.set(idx, Role.ALPHA_WOLF);
            } else if (pool.size() < playerCount) {
                pool.add(Role.ALPHA_WOLF);
            }
        }

        // Add optional human special roles if space remains
        addIfSelected(pool, playerCount, witchBox, Role.WITCH);
        addIfSelected(pool, playerCount, guardBox, Role.GUARD);
        addIfSelected(pool, playerCount, fortuneTellerBox, Role.FORTUNE_TELLER);
        addIfSelected(pool, playerCount, hunterBox, Role.HUNTER);
        addIfSelected(pool, playerCount, knightBox, Role.KNIGHT);

        // Fill remaining with civilians
        while (pool.size() < playerCount) {
            pool.add(Role.CIVILIAN);
        }

        // If user configured too many roles, trim extras safely
        while (pool.size() > playerCount) {
            pool.remove(pool.size() - 1);
        }

        // Shuffle role assignments
        Collections.shuffle(pool, rng);

        return pool;
    }

    private int indexOfFirst(List<Role> roles, Role target) {
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private void addIfSelected(List<Role> pool, int limit, JCheckBox box, Role role) {
        if (!box.isSelected()) {
            return;
        }
        if (pool.size() >= limit) {
            return;
        }
        pool.add(role);
    }

    private List<Player> createPlayers(List<String> names, List<Role> rolePool) {
        List<Player> players = new ArrayList<Player>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Role role = rolePool.get(i);
            players.add(new Player(name, role));
        }
        return players;
    }

    // Action Listeners

    private final class RebuildListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            rebuildNameFields(getPlayerCount());
        }
    }

    private final class ConfirmListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> names = collectNamesOrShowError();
            if (names == null) {
                return;
            }

            int n = names.size();
            List<Role> pool = buildRolePool(n);
            List<Player> players = createPlayers(names, pool);

            createdPlayers = players;

            // Show a brief summary (optional but helpful)
            String summary = buildAssignmentSummary(players);
            JOptionPane.showMessageDialog(
                    InitPanel.this,
                    summary,
                    "Roles Assigned",
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (listener != null) {
                listener.onGameInitialized(players);
            }
        }

        private String buildAssignmentSummary(List<Player> players) {
            StringBuilder sb = new StringBuilder();
            sb.append("Assigned roles:\n\n");
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                sb.append(p.name);
                sb.append(" -> ");
                sb.append(p.getRole().name());
                sb.append("\n");
            }
            return sb.toString();
        }
    }
}
